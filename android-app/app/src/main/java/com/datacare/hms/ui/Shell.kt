@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.datacare.hms.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.datacare.hms.AppViewModel
import com.datacare.hms.data.Roles
import com.datacare.hms.data.UserProfile
import com.datacare.hms.ui.screens.*

/** Bottom navigation: Home, Services, Patients, Appointments, Prescriptions, Billing, Reports, More (from the Task 1 sitemap). */
data class NavItem(val route: String, val label: String, val icon: ImageVector, val page: String?)

val NAV_ITEMS = listOf(
    NavItem("dashboard", "Home", Icons.Default.Home, "dashboard"),
    NavItem("services", "Services", Icons.Default.LocalHospital, "services"),
    NavItem("patients", "Patients", Icons.Default.People, "patient"),
    NavItem("appointments", "Appointments", Icons.Default.Event, "appointments"),
    NavItem("prescriptions", "Prescriptions", Icons.Default.Medication, "prescriptions"),
    NavItem("billing", "Billing", Icons.Default.Receipt, "billing"),
    NavItem("reports", "Reports", Icons.Default.BarChart, "reports"),
    NavItem("more", "More", Icons.Default.MoreHoriz, null)
)

private val TITLES = mapOf(
    "dashboard" to "Dashboard", "services" to "Hospital Services", "patients" to "Patients",
    "patients/new" to "New Patient", "appointments" to "Appointments", "appointments/new" to "New Appointment",
    "prescriptions" to "Prescriptions", "prescriptions/new" to "New Prescription", "billing" to "Billing",
    "billing/new" to "New Invoice", "reports" to "Reports", "more" to "More", "notifications" to "Messages & Alerts",
    "profile" to "My Profile", "settings" to "Settings", "password" to "Change Password", "help" to "Help & Support",
    "users" to "User Management", "about" to "About DataCare"
)

/** Which role permission each route needs (null = open to everyone signed in). */
private fun pageFor(route: String): String? = when (route.substringBefore("/")) {
    "dashboard" -> "dashboard"
    "services" -> "services"
    "patients" -> "patient"
    "appointments" -> "appointments"
    "prescriptions" -> "prescriptions"
    "billing" -> "billing"
    "reports" -> "reports"
    "users" -> "users"
    else -> null
}

@Composable
fun MainShell(vm: AppViewModel, profile: UserProfile, snackbar: SnackbarHostState) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "dashboard"
    val topLevel = NAV_ITEMS.map { it.route }.toSet()
    val items = NAV_ITEMS.filter { it.page == null || Roles.canAccess(profile.role, it.page) }
    val alertsOn by vm.prefs.notifications.collectAsState()
    val busy by vm.busy.collectAsState()

    fun go(r: String) {
        if (r in topLevel) {
            nav.navigate(r) {
                popUpTo("dashboard") { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        } else nav.navigate(r) { launchSingleTop = true }
    }

    @Composable
    fun Guard(page: String?, content: @Composable () -> Unit) {
        if (page == null || Roles.canAccess(profile.role, page)) content() else NoAccess { go("dashboard") }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(TITLES[route] ?: "DataCare HMS", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        if (route !in topLevel) {
                            IconButton(onClick = { nav.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { go("notifications") }) {
                            BadgedBox(badge = { if (alertsOn) Badge() }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Messages and alerts")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        },
        bottomBar = { if (route in topLevel) DcBottomBar(items, route) { go(it) } },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        NavHost(nav, startDestination = "dashboard", modifier = Modifier.padding(pad)) {
            composable("dashboard") { Guard("dashboard") { DashboardScreen(vm, profile, ::go) } }
            composable("services") { Guard("services") { ServicesScreen(vm) } }
            composable("patients") { Guard("patient") { PatientListScreen(vm) { go("patients/new") } } }
            composable("patients/new") { Guard("patient") { PatientFormScreen(vm) { nav.popBackStack() } } }
            composable("appointments") { Guard("appointments") { AppointmentListScreen(vm) { go("appointments/new") } } }
            composable("appointments/new") { Guard("appointments") { AppointmentFormScreen(vm) { nav.popBackStack() } } }
            composable("prescriptions") { Guard("prescriptions") { PrescriptionListScreen(vm) { go("prescriptions/new") } } }
            composable("prescriptions/new") { Guard("prescriptions") { PrescriptionFormScreen(vm) { nav.popBackStack() } } }
            composable("billing") { Guard("billing") { BillingListScreen(vm) { go("billing/new") } } }
            composable("billing/new") { Guard("billing") { InvoiceFormScreen(vm) { nav.popBackStack() } } }
            composable("reports") { Guard("reports") { ReportsScreen(vm) } }
            composable("users") { Guard("users") { UsersScreen(vm) } }
            composable("more") { MoreScreen(vm, profile, ::go) }
            composable("notifications") { NotificationsScreen(vm) }
            composable("profile") { ProfileScreen(vm, profile) }
            composable("settings") { SettingsScreen(vm, profile, ::go) }
            composable("password") { ChangePasswordScreen(vm) { nav.popBackStack() } }
            composable("help") { HelpScreen() }
            composable("about") { AboutScreen() }
        }
    }
}

@Composable
private fun NoAccess(onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        EmptyState("You do not have permission to access this page.", Icons.Default.Lock)
        PrimaryButton("Back to Dashboard", onHome)
    }
}

/** Scrollable so all eight sitemap items stay readable and tappable on small phones. */
@Composable
fun DcBottomBar(items: List<NavItem>, current: String, onSelect: (String) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            items.forEach { item ->
                val selected = current == item.route
                val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    Modifier
                        .width(88.dp)
                        .heightIn(min = 56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(role = Role.Tab) { onSelect(item.route) }
                        .semantics { this.selected = selected; contentDescription = item.label }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.clip(CircleShape)
                            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent)
                            .padding(horizontal = 18.dp, vertical = 4.dp)
                    ) { Icon(item.icon, contentDescription = null, tint = tint) }
                    Text(item.label, fontSize = 12.sp, color = tint, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                }
            }
        }
    }
}
