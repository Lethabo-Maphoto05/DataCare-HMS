package com.datacare.hms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.datacare.hms.data.*
import com.datacare.hms.util.Billing
import com.datacare.hms.util.Format
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** An error whose message is safe and useful to show to the person as-is. */
class UserFacingException(message: String) : Exception(message)

sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val profile: UserProfile) : AuthState
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()
    private val repo = Repository()
    val prefs = Prefs(app)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages

    private var registering = false

    init {
        auth.addAuthStateListener { fa ->
            if (registering) return@addAuthStateListener
            val user = fa.currentUser
            if (user == null) {
                _authState.value = AuthState.SignedOut
            } else {
                viewModelScope.launch {
                    try {
                        _authState.value = AuthState.SignedIn(repo.getOrCreateProfile(user))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        _messages.emit(friendlyError(e))
                        auth.signOut()
                        _authState.value = AuthState.SignedOut
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------- live data

    private val uid: Flow<String?> = _authState
        .map { (it as? AuthState.SignedIn)?.profile?.uid }
        .distinctUntilChanged()

    private fun <T : Any> live(collection: String, clazz: Class<T>): StateFlow<List<T>> =
        uid.flatMapLatest { if (it == null) flowOf(emptyList()) else repo.listen(collection, clazz) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val patients = live(Collections.PATIENTS, Patient::class.java)
    val appointments = live(Collections.APPOINTMENTS, Appointment::class.java)
    val prescriptions = live(Collections.PRESCRIPTIONS, Prescription::class.java)
    val invoices = live(Collections.BILLING, Invoice::class.java)
    val departments = live(Collections.DEPARTMENTS, Department::class.java)

    /** Only System Administrators load the user list (User Management). */
    val users: StateFlow<List<UserProfile>> = _authState.flatMapLatest { s ->
        if (s is AuthState.SignedIn && s.profile.role == Roles.ADMIN) repo.listen(Collections.USERS, UserProfile::class.java)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---------------------------------------------------------------- helpers

    private fun launchAction(
        success: String? = null,
        onDone: (Boolean) -> Unit = {},
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _busy.value = true
            try {
                block()
                success?.let { _messages.emit(it) }
                onDone(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.emit(friendlyError(e))
                onDone(false)
            } finally {
                _busy.value = false
            }
        }
    }

    fun friendlyError(e: Exception): String = when (e) {
        is UserFacingException -> e.message ?: "Something went wrong."
        is FirebaseNetworkException -> "Network error. Please check your internet connection."
        is FirebaseAuthWeakPasswordException -> "Password is too weak. Use at least 6 characters."
        is FirebaseAuthUserCollisionException -> "This email is already registered."
        is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
        is FirebaseAuthRecentLoginRequiredException -> "Please log in again before changing your password."
        is FirebaseFirestoreException ->
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) "You do not have permission to do that."
            else "Database error. Please try again."
        else -> "Something went wrong. Please try again."
    }

    private fun currentUid() = auth.currentUser?.uid.orEmpty()

    // ---------------------------------------------------------------- authentication

    fun signIn(email: String, password: String, onDone: (Boolean) -> Unit = {}) = launchAction(onDone = onDone) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        prefs.rememberedEmail = email.trim()
    }

    /** Firebase Authentication hashes and salts the password server-side; the app never stores it. */
    fun register(first: String, last: String, email: String, password: String, role: String, onDone: (Boolean) -> Unit = {}) {
        launchAction(onDone = onDone) {
            registering = true
            try {
                val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                val user = result.user ?: error("No user returned")
                repo.createProfile(
                    UserProfile(user.uid, first.trim(), last.trim(), email.trim(), role, Format.nowIso())
                )
                auth.signOut() // same as the web app: sign out so the person logs in normally
                _authState.value = AuthState.SignedOut
                _messages.emit("Registration complete! You can now login.")
            } catch (e: Exception) {
                auth.signOut()
                _authState.value = AuthState.SignedOut
                throw e
            } finally {
                registering = false
            }
        }
    }

    fun signInWithGoogle(idToken: String) = launchAction {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
    }

    fun resetPassword(email: String) = launchAction(success = "Password reset email sent. Check your inbox.") {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    fun changePassword(current: String, new: String, onDone: (Boolean) -> Unit) =
        launchAction(success = "Password updated successfully.", onDone = onDone) {
            val user = auth.currentUser ?: error("Not signed in")
            val email = user.email
            if (email.isNullOrBlank() || user.providerData.none { it.providerId == EmailAuthProvider.PROVIDER_ID }) {
                throw UserFacingException("This account signs in with Google, so its password is managed by Google.")
            }
            user.reauthenticate(EmailAuthProvider.getCredential(email, current)).await()
            user.updatePassword(new).await()
        }

    fun signOut() {
        auth.signOut()
    }

    fun showMessage(text: String) {
        _messages.tryEmit(text)
    }

    fun updateName(first: String, last: String, onDone: (Boolean) -> Unit) =
        launchAction(success = "Profile updated.", onDone = onDone) {
            val s = _authState.value as? AuthState.SignedIn ?: return@launchAction
            repo.updateName(s.profile.uid, first.trim(), last.trim())
            _authState.value = AuthState.SignedIn(s.profile.copy(firstName = first.trim(), lastName = last.trim()))
        }

    // ---------------------------------------------------------------- records (field names match the web app)

    private fun stamp() = mapOf("createdBy" to currentUid(), "createdAt" to Format.nowIso())

    fun savePatient(p: Map<String, String>, onDone: (Boolean) -> Unit) =
        launchAction(success = "Patient registered successfully.", onDone = onDone) {
            repo.add(
                Collections.PATIENTS,
                mapOf("patientID" to Format.newId("PAT")) + p + stamp()
            )
        }

    fun saveAppointment(a: Map<String, String>, onDone: (Boolean) -> Unit) =
        launchAction(success = "Appointment saved successfully.", onDone = onDone) {
            repo.add(
                Collections.APPOINTMENTS,
                mapOf("appointmentID" to Format.newId("APT"), "status" to "Confirmed") + a + stamp()
            )
        }

    fun rescheduleAppointment(a: Appointment, date: String, time: String) =
        launchAction(success = "Appointment rescheduled.") {
            repo.update(Collections.APPOINTMENTS, a.docId, mapOf("date" to date, "time" to time, "status" to "Confirmed"))
        }

    fun setAppointmentStatus(a: Appointment, status: String) =
        launchAction(success = "Appointment ${status.lowercase()}.") {
            repo.update(Collections.APPOINTMENTS, a.docId, mapOf("status" to status))
        }

    fun savePrescription(r: Map<String, String>, onDone: (Boolean) -> Unit) =
        launchAction(success = "Prescription saved successfully.", onDone = onDone) {
            repo.add(
                Collections.PRESCRIPTIONS,
                mapOf("prescriptionID" to Format.newId("RX"), "status" to "Active") + r + stamp()
            )
        }

    fun setPrescriptionStatus(r: Prescription, status: String) =
        launchAction(success = "Prescription marked $status.") {
            repo.update(Collections.PRESCRIPTIONS, r.docId, mapOf("status" to status))
        }

    fun saveInvoice(
        patientId: String, invoiceNumber: String, consultation: Double, medication: Double,
        medicalAid: String, paymentMethod: String, outstanding: Double, onDone: (Boolean) -> Unit
    ) = launchAction(success = "Invoice saved successfully.", onDone = onDone) {
        repo.add(
            Collections.BILLING,
            mapOf(
                "invoiceNumber" to invoiceNumber,
                "patientID" to patientId,
                "consultationFee" to consultation,
                "medicationCost" to medication,
                "totalAmount" to Billing.total(consultation, medication),
                "medicalAid" to medicalAid,
                "paymentMethod" to paymentMethod,
                "outstandingBalance" to outstanding,
                "status" to Billing.statusFor(outstanding)
            ) + stamp()
        )
    }

    fun makePayment(inv: Invoice, amount: Double, method: String) =
        launchAction(success = "Payment recorded.") {
            val remaining = Billing.balanceAfterPayment(inv.outstandingBalance, amount)
            repo.update(
                Collections.BILLING, inv.docId,
                mapOf("outstandingBalance" to remaining, "status" to Billing.statusFor(remaining), "paymentMethod" to method)
            )
        }

    fun seedDemoData() = launchAction(success = "Demo data loaded: at least 10 records in every table.") { repo.seed() }
}
