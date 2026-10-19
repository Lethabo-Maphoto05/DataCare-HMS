package com.datacare.hms.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datacare.hms.AppViewModel
import com.datacare.hms.FirebaseConfig
import com.datacare.hms.R
import com.datacare.hms.data.Roles
import com.datacare.hms.data.UserProfile
import com.datacare.hms.ui.*
import com.datacare.hms.ui.theme.*
import com.datacare.hms.util.Format
import com.datacare.hms.util.Validators

// ------------------------------------------------------------------ More menu

@Composable
fun MoreScreen(vm: AppViewModel, profile: UserProfile, go: (String) -> Unit) {
    var confirmLogout by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DcCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(Icons.Default.Person, Blue, BlueLight, 52)
                Column {
                    Text(profile.fullName(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(profile.role, color = Blue, fontWeight = FontWeight.SemiBold)
                    Text(profile.email, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        MenuRow(Icons.Default.Notifications, "Messages & Alerts", "Notifications about appointments, billing and prescriptions") { go("notifications") }
        MenuRow(Icons.Default.AccountCircle, "My Profile", "View and update your details") { go("profile") }
        MenuRow(Icons.Default.Settings, "Settings", "Appearance, text size, notifications") { go("settings") }
        MenuRow(Icons.Default.Key, "Change Password", "Keep your account secure") { go("password") }
        MenuRow(Icons.AutoMirrored.Filled.HelpOutline, "Help & Support", "Guides, FAQs and IT support") { go("help") }
        if (Roles.canAccess(profile.role, "users")) {
            MenuRow(Icons.Default.Group, "User Management", "View registered users and roles") { go("users") }
        }
        MenuRow(Icons.Default.Info, "About DataCare", "Version and privacy information") { go("about") }
        MenuRow(Icons.AutoMirrored.Filled.Logout, "Logout", "Sign out of this device") { confirmLogout = true }
    }
    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Logout?") },
            text = { Text("You will need to sign in again to use DataCare HMS.") },
            confirmButton = { TextButton(onClick = { confirmLogout = false; vm.signOut() }) { Text("Logout") } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Stay signed in") } }
        )
    }
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    DcCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(icon, Blue, BlueLight, 40)
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}

// ------------------------------------------------------------------ Notifications (built from live data)

@Composable
fun NotificationsScreen(vm: AppViewModel) {
    val appointments by vm.appointments.collectAsState()
    val invoices by vm.invoices.collectAsState()
    val prescriptions by vm.prescriptions.collectAsState()
    val today = Format.today()

    data class Alert(val icon: ImageVector, val title: String, val text: String)
    val upcoming = appointments.count { it.status == "Confirmed" && it.date >= today }
    val unpaid = invoices.count { it.outstandingBalance > 0 }
    val active = prescriptions.count { it.status == "Active" }
    val alerts = buildList {
        if (upcoming > 0) add(Alert(Icons.Default.Event, "Upcoming appointments", "$upcoming confirmed appointment(s) from today onwards."))
        if (unpaid > 0) add(Alert(Icons.Default.Receipt, "Outstanding invoices", "$unpaid invoice(s) still have a balance to pay."))
        if (active > 0) add(Alert(Icons.Default.Medication, "Active prescriptions", "$active prescription(s) are currently active."))
        add(Alert(Icons.Default.Security, "Security", "Your account is protected. Never share your password."))
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(alerts) { a ->
            DcCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBadge(a.icon, Blue, BlueLight, 40)
                    Column {
                        Text(a.title, fontWeight = FontWeight.Bold)
                        Text(a.text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Profile

@Composable
fun ProfileScreen(vm: AppViewModel, profile: UserProfile) {
    var first by rememberSaveable { mutableStateOf(profile.firstName) }
    var last by rememberSaveable { mutableStateOf(profile.lastName) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val busy by vm.busy.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DcCard {
            InfoRow("Email", profile.email)
            InfoRow("Role", profile.role)
            InfoRow("Member since", profile.createdAt.take(10))
        }
        DcTextField("First Name", first, { first = it; errors = errors - "first" }, error = errors["first"])
        DcTextField("Last Name", last, { last = it; errors = errors - "last" }, error = errors["last"])
        PrimaryButton(if (busy) "Saving..." else "Save Changes", {
            val e = mutableMapOf<String, String>()
            Validators.requiredError("First name", first)?.let { e["first"] = it }
            Validators.requiredError("Last name", last)?.let { e["last"] = it }
            errors = e
            if (e.isEmpty()) vm.updateName(first, last) {}
        }, Modifier.fillMaxWidth(), Icons.Default.Save, enabled = !busy)
        Text("Your role is set by the hospital and can only be changed by a System Administrator.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ------------------------------------------------------------------ Settings

@Composable
fun SettingsScreen(vm: AppViewModel, profile: UserProfile, go: (String) -> Unit) {
    val theme by vm.prefs.theme.collectAsState()
    val scale by vm.prefs.textScale.collectAsState()
    val alerts by vm.prefs.notifications.collectAsState()
    val busy by vm.busy.collectAsState()
    var confirmSeed by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DcCard {
            SectionTitle("Appearance")
            listOf("system" to "Follow phone setting", "light" to "Light", "dark" to "Dark").forEach { (key, label) ->
                RadioRow(label, theme == key) { vm.prefs.setTheme(key) }
            }
        }
        DcCard {
            SectionTitle("Text size", "Make text easier to read.")
            listOf(1.0f to "Normal", 1.15f to "Large", 1.3f to "Extra large").forEach { (value, label) ->
                RadioRow(label, scale == value) { vm.prefs.setTextScale(value) }
            }
        }
        DcCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Alerts", fontWeight = FontWeight.Bold)
                    Text("Show the alert dot on the bell icon", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = alerts, onCheckedChange = { vm.prefs.setNotifications(it) })
            }
        }
        DcCard {
            SectionTitle("Account")
            SecondaryButton("Change Password", { go("password") }, Modifier.fillMaxWidth(), Icons.Default.Key)
            Spacer(Modifier.height(8.dp))
            SecondaryButton("My Profile", { go("profile") }, Modifier.fillMaxWidth(), Icons.Default.AccountCircle)
            Spacer(Modifier.height(8.dp))
            SecondaryButton("Replay welcome tour", { vm.prefs.setOnboardingDone(false); vm.signOut() }, Modifier.fillMaxWidth(), Icons.Default.Slideshow)
        }
        if (profile.role == Roles.ADMIN) {
            DcCard {
                SectionTitle("Demo data", "Writes 12 patients, appointments, prescriptions and invoices plus 10 departments, staff, inventory items and medical records. Safe to run again.")
                PrimaryButton(if (busy) "Loading..." else "Load Demo Data", { confirmSeed = true }, Modifier.fillMaxWidth(), Icons.Default.CloudUpload, enabled = !busy)
            }
        }
    }
    if (confirmSeed) {
        ConfirmDialog("Load demo data?", "This writes simulated records to the shared DataCare database.", "Load", { vm.seedDemoData() }, { confirmSeed = false })
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouch)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(10.dp))
        Text(label)
    }
}

// ------------------------------------------------------------------ Change password

@Composable
fun ChangePasswordScreen(vm: AppViewModel, onDone: () -> Unit) {
    var current by rememberSaveable { mutableStateOf("") }
    var new by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val busy by vm.busy.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Enter your current password, then choose a new one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        DcTextField("Current password", current, { current = it; errors = errors - "current" }, password = true, error = errors["current"])
        DcTextField("New password", new, { new = it; errors = errors - "new" }, password = true, error = errors["new"])
        DcTextField("Confirm new password", confirm, { confirm = it; errors = errors - "confirm" }, password = true, error = errors["confirm"])
        FormButtons("Update", busy, {
            val e = mutableMapOf<String, String>()
            if (current.isEmpty()) e["current"] = "Current password is required"
            Validators.passwordError(new)?.let { e["new"] = it }
            if (new == current && new.isNotEmpty()) e["new"] = "Choose a different password"
            if (confirm != new) e["confirm"] = "Passwords do not match"
            errors = e
            if (e.isEmpty()) vm.changePassword(current, new) { ok -> if (ok) onDone() }
        }, onDone)
    }
}

// ------------------------------------------------------------------ Users (System Administrator)

@Composable
fun UsersScreen(vm: AppViewModel) {
    val users by vm.users.collectAsState()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (users.isEmpty()) item { EmptyState("No users to show.", Icons.Default.Group) }
        items(users) { u ->
            DcCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBadge(Icons.Default.Person, Purple, PurpleLight, 40)
                    Column(Modifier.weight(1f)) {
                        Text(u.fullName(), fontWeight = FontWeight.Bold)
                        Text(u.email, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusChip(u.role)
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Help & About

private data class Faq(val q: String, val a: String)

private val FAQS = listOf(
    Faq("How do I create an account?", "On the login screen tap Create Account, fill in your name, email, a password of at least 6 characters, and choose your role. Then log in."),
    Faq("I forgot my password.", "Enter your email on the login screen and tap Forgot password? We will email you a reset link."),
    Faq("How do I book an appointment?", "Open Appointments, tap New, fill in the patient, doctor, date, time, type and ward, then tap Save."),
    Faq("How do I reschedule or cancel?", "In Appointments, find the booking and tap Reschedule or Cancel. Only confirmed or pending bookings can be changed."),
    Faq("How do I add a prescription?", "Open Prescriptions, tap New, complete the form and Save. Use Mark Completed when it has been dispensed."),
    Faq("How do I pay an invoice?", "Open Billing, find the invoice with an outstanding balance and tap Make Payment."),
    Faq("Why can't I see some screens?", "The menu depends on your role (for example Pharmacists only see Prescriptions). Ask a System Administrator if your role is wrong."),
    Faq("How do I change the text size or dark mode?", "More > Settings. Choose Light, Dark or Follow phone setting, and Normal, Large or Extra large text."),
    Faq("Is my information safe?", "Yes. Passwords are encrypted by Firebase Authentication and never stored in the app. Data is sent over HTTPS and access depends on your role, in line with POPIA.")
)

@Composable
fun HelpScreen() {
    val ctx = LocalContext.current
    var expanded by rememberSaveable { mutableStateOf(-1) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Frequently asked questions", "Tap a question to see the answer.")
        FAQS.forEachIndexed { i, f ->
            DcCard(onClick = { expanded = if (expanded == i) -1 else i }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(f.q, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Icon(if (expanded == i) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
                if (expanded == i) {
                    Spacer(Modifier.height(6.dp))
                    Text(f.a, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        DcCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(Icons.Default.Headset, Blue, BlueLight)
                Column {
                    Text("Need Help?", fontWeight = FontWeight.Bold)
                    Text("Contact IT Support", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            PrimaryButton("Email IT Support", {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${FirebaseConfig.SUPPORT_EMAIL}"))
                    .putExtra(Intent.EXTRA_SUBJECT, "DataCare HMS app support")
                try { ctx.startActivity(intent) } catch (e: Exception) { /* no email app installed */ }
            }, Modifier.fillMaxWidth(), Icons.Default.Email)
        }
    }
}

@Composable
fun AboutScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(painterResource(R.drawable.datacare_logo), "DataCare logo", Modifier.height(160.dp))
        Text("DataCare Hospital Management System", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Mobile app for Netcare Alberton Hospital - version 1.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
        DcCard {
            InfoRow("Backend", "Firebase Authentication + Cloud Firestore (shared with the web app)")
            InfoRow("Security", "Passwords hashed by Firebase; role-based access; HTTPS")
            InfoRow("Privacy", "Patient data is handled in line with POPIA. Only use records you are authorised to see.")
        }
    }
}