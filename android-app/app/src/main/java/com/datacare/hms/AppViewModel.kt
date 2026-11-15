package com.datacare.hms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.datacare.hms.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val user: AppUser) : AuthState
    data class VerifyEmail(val email: String) : AuthState
    data class CompleteRegistration(val email: String) : AuthState
    data class AwaitingApproval(val message: String) : AuthState
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val auth = FirebaseAuth.getInstance()
    private val repo = ApiRepository(auth)
    private val authResolutionMutex = Mutex()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState
    private val _workspace = MutableStateFlow(WorkspaceState())
    val workspace: StateFlow<WorkspaceState> = _workspace
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 12)
    val messages: SharedFlow<String> = _messages
    private val _selectedInvoice = MutableStateFlow<Invoice?>(null)
    val selectedInvoice: StateFlow<Invoice?> = _selectedInvoice

    init {
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                _authState.value = AuthState.SignedOut
                _workspace.value = WorkspaceState()
                _selectedInvoice.value = null
            } else if (_authState.value !is AuthState.SignedIn && _authState.value !is AuthState.CompleteRegistration && _authState.value !is AuthState.VerifyEmail) {
                viewModelScope.launch { resolveCurrentUser() }
            }
        }
    }

    fun signIn(email: String, password: String) = viewModelScope.launch {
        _busy.value = true
        try {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            resolveCurrentUser()
        } catch (e: Exception) {
            _messages.emit(friendlyError(e))
            if (auth.currentUser != null) auth.signOut()
        } finally { _busy.value = false }
    }

    fun createAccount(email: String, password: String) = action {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: throw IllegalStateException("Firebase did not return a new account.")
        user.sendEmailVerification().await()
        auth.signOut()
        _messages.emit("Account created. Verify your email, then log in to complete your DataCare registration.")
    }

    fun resendVerification() = action {
        val user = auth.currentUser ?: throw IllegalStateException("Please log in again.")
        user.reload().await()
        if (user.isEmailVerified) {
            resolveCurrentUser()
        } else {
            user.sendEmailVerification().await()
            _messages.emit("A new verification email has been sent.")
        }
    }

    fun checkEmailVerification() = action {
        val user = auth.currentUser ?: throw IllegalStateException("Please log in again.")
        user.reload().await()
        if (user.isEmailVerified) resolveCurrentUser()
        else _messages.emit("Your email is not verified yet. Open the verification email and try again.")
    }

    fun completeRegistration(first: String, last: String, phone: String, idNumber: String, password: String) = action {
        val user = auth.currentUser ?: throw IllegalStateException("Please log in again.")
        user.reload().await()
        if (!user.isEmailVerified) throw IllegalStateException("Verify your email address before submitting registration.")
        repo.register(first.trim(), last.trim(), phone.trim(), idNumber.trim(), user.email.orEmpty(), password)
        auth.signOut()
        _messages.emit("Registration submitted. An administrator must approve your account before you can log in.")
    }

    fun resetPassword(email: String) = action {
        auth.sendPasswordResetEmail(email.trim()).await()
        _messages.emit("Password reset email sent. Check your inbox.")
    }

    fun signOut() = auth.signOut()
    fun showMessage(message: String) { _messages.tryEmit(message) }

    private suspend fun resolveCurrentUser() = authResolutionMutex.withLock {
        val firebaseUser = auth.currentUser ?: run { _authState.value = AuthState.SignedOut; return@withLock }
        if ((_authState.value as? AuthState.SignedIn)?.user?.userId == firebaseUser.uid) return@withLock
        try {
            firebaseUser.reload().await()
            if (!firebaseUser.isEmailVerified) {
                _authState.value = AuthState.VerifyEmail(firebaseUser.email.orEmpty())
                return
            }
            val profile = repo.me()
            _authState.value = AuthState.SignedIn(profile)
            refreshWorkspace(profile)
            _messages.emit("Login successful. Welcome to DataCare!")
        } catch (e: ApiException) {
            when {
                e.message.contains("not registered", true) -> _authState.value = AuthState.CompleteRegistration(firebaseUser.email.orEmpty())
                e.message.contains("awaiting administrator approval", true) || e.message.contains("awaiting administrator", true) -> _authState.value = AuthState.AwaitingApproval(e.message)
                e.message.contains("verify your email", true) -> _authState.value = AuthState.VerifyEmail(firebaseUser.email.orEmpty())
                else -> {
                    _messages.emit(e.message)
                    auth.signOut()
                    _authState.value = AuthState.SignedOut
                }
            }
        } catch (e: Exception) {
            _messages.emit(friendlyError(e))
            auth.signOut()
            _authState.value = AuthState.SignedOut
        }
    }

    fun refresh() = viewModelScope.launch {
        val profile = (_authState.value as? AuthState.SignedIn)?.user ?: return@launch
        _loading.value = true
        try { refreshWorkspace(profile) } catch (e: Exception) { _messages.emit(friendlyError(e)) }
        finally { _loading.value = false }
    }

    private suspend fun refreshWorkspace(profile: AppUser) {
        _loading.value = true
        try {
            val notifications = repo.notifications()
            val data = when (profile.role.lowercase()) {
                "admin" -> coroutineScope {
                    val summary = async { repo.summary() }
                    val registrations = async { repo.registrations() }
                    val doctors = async { repo.doctors() }
                    val patients = async { repo.patients() }
                    val wards = async { repo.wards() }
                    val services = async { repo.services() }
                    val medications = async { repo.medications() }
                    val invoices = async { repo.adminInvoices() }
                    WorkspaceState(summary.await(), registrations.await(), patients.await(), doctors.await(), services = services.await(), medications = medications.await(), invoices = invoices.await(), wards = wards.await(), notifications = notifications)
                }
                "doctor" -> coroutineScope {
                    val appointments = async { repo.doctorAppointments() }
                    val patients = async { repo.patients() }
                    val services = async { repo.services() }
                    val medications = async { repo.medications() }
                    val doctors = async { repo.doctors() }
                    WorkspaceState(patients = patients.await(), doctors = doctors.await(), appointments = appointments.await(), services = services.await(), medications = medications.await(), notifications = notifications)
                }
                "patient" -> coroutineScope {
                    val patient = async { repo.myPatientProfile() }
                    val doctors = async { repo.doctors() }
                    val appointments = async { repo.myAppointments() }
                    val prescriptions = async { repo.myPrescriptions() }
                    val invoices = async { repo.myInvoices() }
                    val services = async { repo.services() }
                    WorkspaceState(doctors = doctors.await(), appointments = appointments.await(), prescriptions = prescriptions.await(), invoices = invoices.await(), services = services.await(), patientProfile = patient.await(), notifications = notifications)
                }
                else -> throw IllegalStateException("This DataCare account has an unsupported role.")
            }
            _workspace.value = data
        } finally { _loading.value = false }
    }

    private fun action(success: String? = null, block: suspend () -> Unit) = viewModelScope.launch {
        _busy.value = true
        try {
            block()
            if (success != null) _messages.emit(success)
            val profile = (_authState.value as? AuthState.SignedIn)?.user
            if (profile != null) refreshWorkspace(profile)
        } catch (e: Exception) {
            _messages.emit(friendlyError(e))
        } finally { _busy.value = false }
    }

    fun approveRegistration(id: String, approve: Boolean) = action(if (approve) "Patient registration approved." else "Patient registration rejected.") {
        repo.updateRegistration(id, if (approve) "Approved" else "Rejected")
    }

    fun createDoctor(first: String, last: String, email: String, phone: String, specialisation: String, department: String, password: String) = action("Doctor account created successfully.") {
        repo.createDoctor(first, last, email, phone, specialisation, department, password)
    }

    fun createPatient(first: String, last: String, phone: String, idNumber: String) = action("Patient record added successfully.") {
        repo.createPatient(first, last, phone, idNumber)
    }

    fun createWard(name: String, bed: Int) = action("Ward bed added successfully.") { repo.createWard(name, bed) }
    fun admitPatient(patientId: Int, wardId: Int, doctorId: Int?, date: String, notes: String) = action("Patient allocated to the ward successfully.") {
        repo.admit(patientId, wardId, doctorId, date, notes)
    }
    fun bookAdminAppointment(patientId: Int, doctorId: Int, date: String, time: String) = action("Appointment booked and patient notified.") {
        repo.bookAdminAppointment(patientId, doctorId, date, time)
    }
    fun requestAppointment(doctorId: Int, date: String, time: String) = action("Appointment request sent to the doctor.") {
        repo.requestAppointment(doctorId, date, time)
    }
    fun confirmAppointment(id: Int, date: String, time: String, ward: String) = action("Appointment confirmed; patient notified.") {
        repo.confirmAppointment(id, date, time, ward)
    }
    fun completeAppointment(id: Int) = action("Appointment marked as completed.") { repo.appointmentStatus(id, "Completed") }
    fun createPrescription(patientId: Int, doctorId: Int?, serviceId: Int, medicationId: Int, quantity: Int, dosage: String, notes: String) = action("Prescription issued and bill created.") {
        repo.createPrescription(patientId, doctorId, serviceId, medicationId, quantity, dosage, notes)
    }

    fun openInvoice(id: Int) = viewModelScope.launch {
        _busy.value = true
        try { _selectedInvoice.value = repo.invoice(id) }
        catch (e: Exception) { _messages.emit(friendlyError(e)) }
        finally { _busy.value = false }
    }
    fun closeInvoice() { _selectedInvoice.value = null }
    fun payInvoice(id: Int, method: String, aidName: String, aidNumber: String, medication: Boolean, delivery: Boolean, address: String) = action {
        val result = repo.payInvoice(id, method, aidName, aidNumber, medication, delivery, address)
        _messages.emit(result.message)
        _selectedInvoice.value = null
    }
    fun markNotificationRead(id: Int) = action { repo.markNotificationRead(id) }

    private fun friendlyError(e: Exception): String = when (e) {
        is ApiException -> e.message
        is FirebaseNetworkException -> "Connection problem. Check your internet connection and try again."
        is FirebaseAuthWeakPasswordException -> "Choose a stronger password."
        is FirebaseAuthUserCollisionException -> "An account already exists with this email address."
        is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
        else -> e.message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
    }
}

