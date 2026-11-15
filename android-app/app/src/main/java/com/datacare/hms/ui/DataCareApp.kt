package com.datacare.hms.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import com.datacare.hms.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datacare.hms.AppViewModel
import com.datacare.hms.AuthState
import com.datacare.hms.data.*
import kotlinx.coroutines.launch
import java.util.Locale

private val Navy = Color(0xFF102B4E)
private val Blue = Color(0xFF1769AA)
private val Pale = Color(0xFFF2F6FB)

@Composable
fun DataCareApp(vm: AppViewModel) {
    val auth by vm.authState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue, onPrimary = Color.White, secondary = Color(0xFF15956B),
            background = Pale, surface = Color.White, onSurface = Navy,
            onBackground = Navy, error = Color(0xFFB42318)
        )
    ) {
        when (val state = auth) {
            AuthState.Loading -> CenterMessage("Loading DataCare HMS…", snackbar)
            AuthState.SignedOut -> AuthScreen(vm, snackbar)
            is AuthState.VerifyEmail -> VerifyEmailScreen(vm, state.email, snackbar)
            is AuthState.CompleteRegistration -> CompleteRegistrationScreen(vm, state.email, snackbar)
            is AuthState.AwaitingApproval -> AwaitingApprovalScreen(vm, state.message, snackbar)
            is AuthState.SignedIn -> WorkspaceScreen(vm, state.user, snackbar)
        }
    }
}

@Composable
private fun CenterMessage(message: String, snackbar: SnackbarHostState) {
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator()
                Text(message, color = Navy)
            }
        }
    }
}

@Composable
private fun AuthScreen(vm: AppViewModel, snackbar: SnackbarHostState) {
    var register by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val busy by vm.busy.collectAsState()
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Spacer(Modifier.height(20.dp))
            Box(Modifier.fillMaxWidth().background(Navy, RoundedCornerShape(22.dp)).padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Image(painterResource(R.drawable.datacare_logo), contentDescription = "DataCare logo", modifier = Modifier.size(68.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("DataCare HMS", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text("Hospital Management System", color = Color.White.copy(alpha = .9f))
                        Text("Secure access to your hospital workspace", color = Color.White.copy(alpha = .8f), fontSize = 12.sp)
                    }
                }
            }
            Text(if (register) "Create patient account" else "Welcome back", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(if (register) "Create your login credentials first. After email verification, you will complete your patient details and wait for administrator approval." else "Sign in using your existing DataCare account.", color = Color.DarkGray)
            FormField("Email address", email, { email = it }, KeyboardType.Email)
            FormField("Password", password, { password = it }, password = true)
            if (register) {
                Text("Password must contain at least 10 characters, uppercase and lowercase letters, a number and a special character.", fontSize = 12.sp, color = Color.DarkGray)
                PrimaryAction("Create account", busy) {
                    val strong = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,128}$")
                    when {
                        !email.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[A-Za-z]{2,}$")) -> vm.showMessage("Enter a valid email address.")
                        !strong.matches(password) -> vm.showMessage("Password does not meet the required strength.")
                        else -> vm.createAccount(email, password)
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { if (email.isBlank()) vm.showMessage("Enter your email address first.") else vm.resetPassword(email) }) { Text("Forgot password?") }
                }
                PrimaryAction("Login", busy) {
                    if (!email.contains("@") || password.isBlank()) vm.showMessage("Enter a valid email address and password.")
                    else vm.signIn(email, password)
                }
            }
            OutlinedButton(onClick = { register = !register; password = "" }, modifier = Modifier.fillMaxWidth()) {
                Text(if (register) "Already registered? Login" else "Create a patient account")
            }
            Text("Only administrators can create doctor accounts and assign system roles.", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun VerifyEmailScreen(vm: AppViewModel, email: String, snackbar: SnackbarHostState) {
    val busy by vm.busy.collectAsState()
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.MarkEmailRead, null, tint = Blue, modifier = Modifier.size(64.dp))
            Text("Verify your email", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("We need you to verify $email before you can continue. Open the verification email, then return here.", color = Color.DarkGray)
            PrimaryAction("I've verified my email", busy) { vm.checkEmailVerification() }
            OutlinedButton(onClick = { vm.resendVerification() }, modifier = Modifier.fillMaxWidth()) { Text("Resend verification email") }
            TextButton(onClick = { vm.signOut() }) { Text("Back to login") }
        }
    }
}

@Composable
private fun CompleteRegistrationScreen(vm: AppViewModel, email: String, snackbar: SnackbarHostState) {
    var first by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var id by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val busy by vm.busy.collectAsState()
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Complete your registration", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("Your email $email is verified. Add the details required by DataCare. Your account will remain pending until an administrator approves it.", color = Color.DarkGray)
            FormField("First name", first, { first = it })
            FormField("Surname", last, { last = it })
            FormField("South African cell number", phone, { phone = it }, KeyboardType.Phone)
            FormField("South African ID number (13 digits)", id, { id = it }, KeyboardType.Number)
            FormField("Re-enter your password", password, { password = it }, password = true)
            Text("Your password is sent to the API only for strength validation and is not saved in the DataCare database.", fontSize = 12.sp, color = Color.Gray)
            PrimaryAction("Submit registration", busy) {
                val p = phone.replace(Regex("[\\s()-]"), "")
                val strong = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,128}$")
                when {
                    first.isBlank() || last.isBlank() -> vm.showMessage("Enter your first name and surname.")
                    !p.matches(Regex("^(?:0[678]\\d{8}|\\+27[678]\\d{8})$")) -> vm.showMessage("Enter a valid South African cell number.")
                    !id.matches(Regex("^\\d{13}$")) -> vm.showMessage("South African ID must contain exactly 13 digits.")
                    !strong.matches(password) -> vm.showMessage("Re-enter the strong password used when creating your account.")
                    else -> vm.completeRegistration(first, last, phone, id, password)
                }
            }
            TextButton(onClick = { vm.signOut() }) { Text("Logout") }
        }
    }
}

@Composable
private fun AwaitingApprovalScreen(vm: AppViewModel, message: String, snackbar: SnackbarHostState) {
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.HourglassTop, null, tint = Blue, modifier = Modifier.size(64.dp))
            Text("Registration awaiting approval", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(message, color = Color.DarkGray)
            Text("You can sign in again after an administrator approves your account.")
            Button(onClick = { vm.signOut() }) { Text("Logout") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkspaceScreen(vm: AppViewModel, user: AppUser, snackbar: SnackbarHostState) {
    val data by vm.workspace.collectAsState()
    val busy by vm.busy.collectAsState()
    val loading by vm.loading.collectAsState()
    val selectedInvoice by vm.selectedInvoice.collectAsState()
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val pages = RoleNavigation.pagesFor(user.role)
    var page by rememberSaveable(user.role) { mutableStateOf("Dashboard") }
    var showSupport by remember { mutableStateOf(false) }
    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.fillMaxWidth().background(Navy).padding(20.dp)) {
                    Text("DataCare HMS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 23.sp)
                    Text(user.fullName.ifBlank { user.email }, color = Color.White.copy(alpha = .85f))
                    Text(user.role.uppercase(Locale.getDefault()), color = Color.White.copy(alpha = .7f), fontSize = 12.sp)
                }
                pages.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item) },
                        selected = page == item,
                        onClick = { page = item; scope.launch { drawer.close() } },
                        icon = { Icon(pageIcon(item), null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(label = { Text("Need Help? Contact IT Support") }, selected = false, onClick = { showSupport = true; scope.launch { drawer.close() } }, icon = { Icon(Icons.Default.SupportAgent, null) })
                NavigationDrawerItem(label = { Text("Logout") }, selected = false, onClick = { vm.signOut() }, icon = { Icon(Icons.Default.Logout, null) })
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(
                    title = { Column { Text(page, fontWeight = FontWeight.Bold); Text(user.fullName.ifBlank { user.email }, fontSize = 11.sp, color = Color.Gray) } },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawer.open() } }) { Icon(Icons.Default.Menu, "Menu") } },
                    actions = {
                        IconButton(onClick = { vm.refresh() }, enabled = !loading) { if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Icon(Icons.Default.Refresh, "Refresh") }
                        IconButton(onClick = { vm.signOut() }) { Icon(Icons.Default.Logout, "Logout") }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (page) {
                    "Dashboard" -> DashboardPage(vm, user, data)
                    "Patients" -> AdminPatientsPage(vm, data)
                    "Appointments" -> AppointmentsPage(vm, user, data)
                    "Prescriptions" -> PrescriptionsPage(vm, user, data)
                    "Billing" -> BillingPage(vm, user, data, selectedInvoice)
                    "Services" -> ServicesPage(data)
                    "Reports" -> ReportsPage(data)
                    "User Management" -> UserManagementPage(vm, data)
                }
            }
        }
    }
    if (showSupport) AlertDialog(
        onDismissRequest = { showSupport = false },
        title = { Text("IT Support") },
        text = { Text("Need assistance? Contact your system administrator for help.") },
        confirmButton = { TextButton(onClick = { showSupport = false }) { Text("OK") } },
        dismissButton = { TextButton(onClick = { showSupport = false }) { Text("Close") } }
    )
}

@Composable
private fun DashboardPage(vm: AppViewModel, user: AppUser, data: WorkspaceState) {
    PageColumn {
        Text("Welcome, ${user.fullName.ifBlank { user.email }}", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Your secure hospital management workspace", color = Color.Gray)
        when (user.role.lowercase()) {
            "admin" -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("Patients", data.summary.patients.toString(), Modifier.weight(1f))
                    StatTile("Doctors", data.summary.doctors.toString(), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("Appointments", data.summary.appointments.toString(), Modifier.weight(1f))
                    StatTile("Prescriptions", data.summary.prescriptions.toString(), Modifier.weight(1f))
                }
                StatTile("Recorded revenue", money(data.summary.revenue))
                SectionHeading("Pending patient registrations", "Approve or reject new patient account requests.")
                if (data.registrations.isEmpty()) EmptyCard("There are no pending registrations.")
                data.registrations.forEach { registration ->
                    DataCard {
                        Text("${registration.firstName} ${registration.lastName}", fontWeight = FontWeight.Bold)
                        Text(registration.email)
                        Text("Cell: ${registration.phoneNumber}")
                        Text("ID number: ${registration.idNumber}")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { vm.approveRegistration(registration.userId, true) }) { Text("Approve") }
                            OutlinedButton(onClick = { vm.approveRegistration(registration.userId, false) }) { Text("Reject") }
                        }
                    }
                }
            }
            "doctor" -> {
                StatTile("Your appointments", data.appointments.size.toString())
                StatTile("Pending requests", data.appointments.count { it.status == "Pending" }.toString())
                SectionHeading("Upcoming work", "Your assigned appointments.")
                data.appointments.take(5).forEach { AppointmentCard(it) }
            }
            else -> {
                data.patientProfile?.let { p ->
                    DataCard {
                        Text("My patient details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        InfoLine("Patient number", p.patientNumber)
                        InfoLine("Name", p.displayName)
                        InfoLine("Cell", p.phoneNumber)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("Appointments", data.appointments.size.toString(), Modifier.weight(1f))
                    StatTile("Invoices", data.invoices.size.toString(), Modifier.weight(1f))
                }
            }
        }
        SectionHeading("Notifications", "Updates from your DataCare account.")
        if (data.notifications.isEmpty()) EmptyCard("You have no notifications.")
        data.notifications.forEach { n ->
            DataCard {
                Text(n.message)
                Text(n.createdAt, fontSize = 11.sp, color = Color.Gray)
                if (!n.isRead) TextButton(onClick = { vm.markNotificationRead(n.notificationId) }) { Text("Mark as read") }
            }
        }
    }
}

@Composable
private fun AdminPatientsPage(vm: AppViewModel, data: WorkspaceState) {
    var first by rememberSaveable { mutableStateOf("") }; var last by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }; var id by rememberSaveable { mutableStateOf("") }
    var wardName by rememberSaveable { mutableStateOf("") }; var bed by rememberSaveable { mutableStateOf("") }
    var patientId by rememberSaveable { mutableStateOf<Int?>(null) }; var wardId by rememberSaveable { mutableStateOf<Int?>(null) }
    var doctorId by rememberSaveable { mutableStateOf<Int?>(null) }; var date by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    PageColumn {
        SectionHeading("Register a patient", "Creates a hospital patient record, not a login account.")
        DataCard {
            FormField("First name", first, { first = it }); FormField("Surname", last, { last = it })
            FormField("South African cell number", phone, { phone = it }, KeyboardType.Phone)
            FormField("South African ID number", id, { id = it }, KeyboardType.Number)
            PrimaryAction("Add patient") { if (first.isBlank() || last.isBlank() || phone.isBlank() || id.length != 13) vm.showMessage("Complete all patient fields with a valid 13-digit ID.") else vm.createPatient(first, last, phone, id) }
        }
        SectionHeading("Patient directory", "Patients managed by the hospital.")
        if (data.patients.isEmpty()) EmptyCard("No patient records found.")
        data.patients.forEach { p -> DataCard { Text(p.displayName, fontWeight = FontWeight.Bold); InfoLine("Patient number", p.patientNumber); InfoLine("Cell", p.phoneNumber) } }
        SectionHeading("Add a ward bed", "Create a new ward bed for patient allocation.")
        DataCard {
            FormField("Ward name", wardName, { wardName = it }); FormField("Bed number", bed, { bed = it }, KeyboardType.Number)
            PrimaryAction("Add bed") { val b = bed.toIntOrNull(); if (wardName.isBlank() || b == null || b < 1) vm.showMessage("Enter a ward name and a positive bed number.") else vm.createWard(wardName, b) }
        }
        SectionHeading("Allocate a patient to a ward", "Only available beds can be allocated.")
        DataCard {
            SelectInt("Patient", data.patients.map { it.patientId to "${it.patientNumber} — ${it.displayName}" }, patientId, { patientId = it })
            SelectInt("Available bed", data.wards.filter { it.status == "Available" }.map { it.wardId to it.label }, wardId, { wardId = it })
            SelectInt("Responsible doctor (optional)", data.doctors.map { it.doctorId to "${it.fullName} — ${it.specialisation}" }, doctorId, { doctorId = it })
            FormField("Admission date (YYYY-MM-DD)", date, { date = it })
            FormField("Notes (optional)", notes, { notes = it })
            PrimaryAction("Allocate ward") { if (patientId == null || wardId == null || date.isBlank()) vm.showMessage("Choose a patient, an available bed and an admission date.") else vm.admitPatient(patientId!!, wardId!!, doctorId, date, notes) }
        }
        SectionHeading("Ward availability", "Current bed status.")
        data.wards.forEach { w -> DataCard { Text(w.wardName, fontWeight = FontWeight.Bold); InfoLine("Bed", w.bedNumber.toString()); InfoLine("Status", w.status) } }
    }
}

@Composable
private fun AppointmentsPage(vm: AppViewModel, user: AppUser, data: WorkspaceState) {
    if (user.role == "admin") {
        var patientId by rememberSaveable { mutableStateOf<Int?>(null) }; var doctorId by rememberSaveable { mutableStateOf<Int?>(null) }
        var date by rememberSaveable { mutableStateOf("") }; var time by rememberSaveable { mutableStateOf("") }
        PageColumn {
            SectionHeading("Book an appointment", "Administrators can book an appointment for a patient.")
            DataCard {
                SelectInt("Patient", data.patients.map { it.patientId to "${it.patientNumber} — ${it.displayName}" }, patientId, { patientId = it })
                SelectInt("Doctor", data.doctors.map { it.doctorId to "${it.fullName} — ${it.specialisation}" }, doctorId, { doctorId = it })
                FormField("Appointment date (YYYY-MM-DD)", date, { date = it })
                FormField("Appointment time (HH:MM)", time, { time = it })
                PrimaryAction("Book appointment") { if (patientId == null || doctorId == null || date.isBlank() || time.isBlank()) vm.showMessage("Complete all appointment details.") else vm.bookAdminAppointment(patientId!!, doctorId!!, date, time) }
            }
        }
    } else if (user.role == "doctor") {
        PageColumn {
            SectionHeading("Appointment requests and schedule", "Confirm pending requests or complete confirmed appointments.")
            if (data.appointments.isEmpty()) EmptyCard("You have no appointments.")
            data.appointments.forEach { appointment ->
                var date by rememberSaveable(appointment.appointmentId) { mutableStateOf(appointment.appointmentDate) }
                var time by rememberSaveable(appointment.appointmentId) { mutableStateOf(appointment.appointmentTime.take(5)) }
                var ward by rememberSaveable(appointment.appointmentId) { mutableStateOf(appointment.wardAllocation.orEmpty()) }
                DataCard {
                    Text(appointment.patientName ?: "Patient #${appointment.patientId}", fontWeight = FontWeight.Bold)
                    InfoLine("Patient number", appointment.patientNumber.orEmpty())
                    InfoLine("Date", appointment.appointmentDate); InfoLine("Time", appointment.appointmentTime)
                    InfoLine("Status", appointment.status)
                    if (appointment.status == "Pending") {
                        FormField("Confirm date", date, { date = it }); FormField("Confirm time", time, { time = it }); FormField("Ward / location", ward, { ward = it })
                        PrimaryAction("Confirm appointment") { vm.confirmAppointment(appointment.appointmentId, date, time, ward) }
                    } else if (appointment.status == "Confirmed") {
                        Button(onClick = { vm.completeAppointment(appointment.appointmentId) }) { Text("Mark completed") }
                    }
                }
            }
        }
    } else {
        var doctorId by rememberSaveable { mutableStateOf<Int?>(null) }; var date by rememberSaveable { mutableStateOf("") }; var time by rememberSaveable { mutableStateOf("") }
        PageColumn {
            SectionHeading("Request an appointment", "Your appointment is not confirmed until the doctor accepts it.")
            DataCard {
                SelectInt("Choose a doctor", data.doctors.map { it.doctorId to "${it.fullName} — ${it.specialisation}" }, doctorId, { doctorId = it })
                FormField("Preferred date (YYYY-MM-DD)", date, { date = it }); FormField("Preferred time (HH:MM)", time, { time = it })
                PrimaryAction("Send appointment request") { if (doctorId == null || date.isBlank() || time.isBlank()) vm.showMessage("Choose a doctor, date and time.") else vm.requestAppointment(doctorId!!, date, time) }
            }
            SectionHeading("My appointments", "Your appointment requests and confirmed visits.")
            if (data.appointments.isEmpty()) EmptyCard("You have no appointments yet.")
            data.appointments.forEach { AppointmentCard(it) }
        }
    }
}

@Composable
private fun PrescriptionsPage(vm: AppViewModel, user: AppUser, data: WorkspaceState) {
    if (user.role == "patient") {
        PageColumn {
            SectionHeading("My prescriptions", "Prescriptions issued by your doctor.")
            if (data.prescriptions.isEmpty()) EmptyCard("No prescriptions found.")
            data.prescriptions.forEach { p ->
                DataCard {
                    Text("Prescription #${p.prescriptionId}", fontWeight = FontWeight.Bold)
                    InfoLine("Date", p.prescriptionDate); InfoLine("Doctor", p.doctorName.orEmpty())
                    p.items.forEach { item -> Text("${item.medication} ${item.strength.orEmpty()} — ${item.dosage} (x${item.quantity})") }
                    p.notes?.takeIf { it.isNotBlank() }?.let { Text("Notes: $it", color = Color.Gray) }
                }
            }
        }
    } else {
        var patientId by rememberSaveable { mutableStateOf<Int?>(null) }; var doctorId by rememberSaveable { mutableStateOf<Int?>(user.doctorId) }
        var serviceId by rememberSaveable { mutableStateOf<Int?>(null) }; var medicationId by rememberSaveable { mutableStateOf<Int?>(null) }
        var quantity by rememberSaveable { mutableStateOf("1") }; var dosage by rememberSaveable { mutableStateOf("") }; var notes by rememberSaveable { mutableStateOf("") }
        PageColumn {
            SectionHeading("Issue prescription and create bill", "Select the patient, consultation service and medication.")
            DataCard {
                SelectInt("Patient", data.patients.map { it.patientId to "${it.patientNumber} — ${it.displayName}" }, patientId, { patientId = it })
                if (user.role == "admin") SelectInt("Doctor", data.doctors.map { it.doctorId to it.fullName }, doctorId, { doctorId = it })
                SelectInt("Consultation / service", data.services.map { it.serviceId to "${it.name} — ${money(it.price)}" }, serviceId, { serviceId = it })
                SelectInt("Medication", data.medications.map { it.medicationId to "${it.name} ${it.strength.orEmpty()} — ${money(it.unitPrice)} (stock ${it.stockQty})" }, medicationId, { medicationId = it })
                FormField("Quantity", quantity, { quantity = it }, KeyboardType.Number)
                FormField("Dosage instructions", dosage, { dosage = it })
                FormField("Notes (optional)", notes, { notes = it })
                PrimaryAction("Issue prescription") {
                    val q = quantity.toIntOrNull()
                    if (patientId == null || serviceId == null || medicationId == null || q == null || q < 1 || dosage.isBlank() || (user.role == "admin" && doctorId == null)) vm.showMessage("Complete all prescription details with a valid quantity.")
                    else vm.createPrescription(patientId!!, if (user.role == "admin") doctorId else null, serviceId!!, medicationId!!, q, dosage, notes)
                }
                Text("A consultation invoice is created by the API. Medication is added to the bill when the patient selects it during payment.", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun BillingPage(vm: AppViewModel, user: AppUser, data: WorkspaceState, selectedInvoice: Invoice?) {
    var paymentMethod by rememberSaveable { mutableStateOf("Online Demo") }
    var aidName by rememberSaveable { mutableStateOf("") }; var aidNumber by rememberSaveable { mutableStateOf("") }
    var includeMedication by rememberSaveable { mutableStateOf(false) }; var delivery by rememberSaveable { mutableStateOf(false) }; var address by rememberSaveable { mutableStateOf("") }
    PageColumn {
        SectionHeading(if (user.role == "admin") "Billing records" else "My bills", "Invoice and payment information.")
        if (data.invoices.isEmpty()) EmptyCard("No invoices found.")
        data.invoices.forEach { invoice ->
            DataCard {
                Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold)
                if (user.role == "admin") InfoLine("Patient", "${invoice.patientNumber.orEmpty()} ${invoice.patientName.orEmpty()}")
                InfoLine("Date", invoice.billingDate); InfoLine("Total", money(invoice.total)); InfoLine("Status", invoice.paymentStatus)
                if (user.role == "patient" && invoice.paymentStatus != "Paid") Button(onClick = { vm.openInvoice(invoice.invoiceId) }) { Text("View / Pay") }
            }
        }
        if (user.role == "patient" && selectedInvoice != null) {
            SectionHeading("Invoice ${selectedInvoice.invoiceNumber}", "Review the bill before confirming payment.")
            DataCard {
                selectedInvoice.lines.forEach { line ->
                    InfoLine(line.description, "${line.quantity} × ${money(line.unitPrice)}")
                }
                HorizontalDivider()
                InfoLine("Total", money(selectedInvoice.total))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(includeMedication, { includeMedication = it; if (!it) delivery = false })
                    Text("Add prescribed medication to this bill")
                }
                Text("Payment method", fontWeight = FontWeight.SemiBold)
                SelectString(listOf("Online Demo", "Medical Aid"), paymentMethod, { paymentMethod = it })
                if (paymentMethod == "Medical Aid") {
                    FormField("Medical aid name", aidName, { aidName = it })
                    FormField("Medical aid membership number", aidNumber, { aidNumber = it })
                }
                if (includeMedication) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(delivery, { delivery = it }); Text("Deliver medication to my address")
                    }
                    if (delivery) FormField("Delivery address", address, { address = it })
                }
                Text("Demonstration only: no real bank/card transaction takes place.", color = Color(0xFF805A00), fontSize = 12.sp)
                PrimaryAction("Confirm payment") {
                    if (paymentMethod == "Medical Aid" && (aidName.isBlank() || aidNumber.isBlank())) vm.showMessage("Enter your medical aid name and membership number.")
                    else if (delivery && address.isBlank()) vm.showMessage("Enter the delivery address.")
                    else vm.payInvoice(selectedInvoice.invoiceId, paymentMethod, aidName, aidNumber, includeMedication, delivery, address)
                }
                TextButton(onClick = { vm.closeInvoice() }) { Text("Close invoice") }
            }
        }
    }
}

@Composable
private fun ServicesPage(data: WorkspaceState) {
    PageColumn {
        SectionHeading("Hospital Services", "Services offered by DataCare.")
        if (data.services.isEmpty()) EmptyCard("No services are currently listed.")
        data.services.forEach { s -> DataCard {
            Text(s.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(s.description.orEmpty(), color = Color.DarkGray)
            InfoLine("Price", money(s.price))
        } }
    }
}

@Composable
private fun ReportsPage(data: WorkspaceState) {
    PageColumn {
        SectionHeading("Reports & Analytics", "Hospital activity summary from the shared DataCare API.")
        StatTile("Patients", data.summary.patients.toString())
        StatTile("Doctors", data.summary.doctors.toString())
        StatTile("Appointments", data.summary.appointments.toString())
        StatTile("Prescriptions", data.summary.prescriptions.toString())
        StatTile("Recorded revenue", money(data.summary.revenue))
    }
}

@Composable
private fun UserManagementPage(vm: AppViewModel, data: WorkspaceState) {
    var first by rememberSaveable { mutableStateOf("") }; var last by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }; var phone by rememberSaveable { mutableStateOf("") }
    var specialisation by rememberSaveable { mutableStateOf("") }; var department by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    PageColumn {
        SectionHeading("Add a doctor", "Doctor accounts are created by an administrator.")
        DataCard {
            FormField("First name", first, { first = it }); FormField("Surname", last, { last = it })
            FormField("Email address", email, { email = it }, KeyboardType.Email); FormField("South African cell number", phone, { phone = it }, KeyboardType.Phone)
            FormField("Specialisation", specialisation, { specialisation = it }); FormField("Department", department, { department = it })
            FormField("Temporary password (10+ characters)", password, { password = it }, password = true)
            PrimaryAction("Create doctor account") {
                val strong = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,128}$")
                if (first.isBlank() || last.isBlank() || !email.contains("@") || phone.isBlank() || specialisation.isBlank() || department.isBlank() || !strong.matches(password)) vm.showMessage("Complete all doctor details and use a strong temporary password.")
                else vm.createDoctor(first, last, email, phone, specialisation, department, password)
            }
            Text("Share the temporary password securely. The doctor should change it after first login.", fontSize = 12.sp, color = Color.Gray)
        }
        SectionHeading("Doctors", "Doctor accounts registered in DataCare.")
        data.doctors.forEach { d -> DataCard { Text(d.fullName, fontWeight = FontWeight.Bold); InfoLine("Specialisation", d.specialisation); InfoLine("Availability", d.availability); InfoLine("Email", d.email.orEmpty()) } }
    }
}

@Composable
private fun AppointmentCard(a: Appointment) {
    DataCard {
        Text(a.doctorName ?: a.patientName ?: "Appointment #${a.appointmentId}", fontWeight = FontWeight.Bold)
        InfoLine("Date", a.appointmentDate); InfoLine("Time", a.appointmentTime); InfoLine("Status", a.status)
        a.wardAllocation?.takeIf { it.isNotBlank() }?.let { InfoLine("Location", it) }
    }
}

@Composable
private fun PageColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
}

@Composable
private fun DataCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp), content = content)
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text(subtitle, fontSize = 13.sp, color = Color.Gray)
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(value, color = Blue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyCard(message: String) { DataCard { Text(message, color = Color.Gray) } }

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Text(value.ifBlank { "—" }, color = Navy, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit, keyboard: KeyboardType = KeyboardType.Text, password: Boolean = false) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard),
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}

@Composable
private fun PrimaryAction(label: String, busy: Boolean = false, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
        if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White) else Text(label)
    }
}

@Composable
private fun SelectInt(label: String, options: List<Pair<Int, String>>, selected: Int?, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: "Choose…"
    Column {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedLabel, modifier = Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (id, text) -> DropdownMenuItem(text = { Text(text) }, onClick = { onSelected(id); expanded = false }) }
            }
        }
    }
}

@Composable
private fun SelectString(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(selected, modifier = Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { onSelected(item); expanded = false }) }
        }
    }
}

private fun pageIcon(page: String) = when (page) {
    "Dashboard" -> Icons.Default.Dashboard
    "Patients" -> Icons.Default.Groups
    "Appointments" -> Icons.Default.CalendarMonth
    "Prescriptions" -> Icons.Default.Medication
    "Billing" -> Icons.Default.ReceiptLong
    "Services" -> Icons.Default.MedicalServices
    "Reports" -> Icons.Default.Analytics
    "User Management" -> Icons.Default.ManageAccounts
    else -> Icons.Default.Home
}

private fun money(amount: Double): String = "R" + String.format(Locale.US, "%.2f", amount)
