package com.datacare.hms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datacare.hms.AppViewModel
import com.datacare.hms.data.Roles
import com.datacare.hms.data.UserProfile
import com.datacare.hms.ui.*
import com.datacare.hms.ui.theme.*
import com.datacare.hms.util.Format
import com.datacare.hms.util.Reports

@Composable
fun DashboardScreen(vm: AppViewModel, profile: UserProfile, go: (String) -> Unit) {
    val patients by vm.patients.collectAsState()
    val appointments by vm.appointments.collectAsState()
    val prescriptions by vm.prescriptions.collectAsState()
    val invoices by vm.invoices.collectAsState()
    val allowed = Roles.pagesFor(profile.role)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(BlueDark, Blue, Green)))
                .padding(20.dp)
        ) {
            Column {
                Text(Format.greeting(), color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                Text(profile.fullName(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, null, Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text(profile.role, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(Format.today(), color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text("Hospital Management Dashboard", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Total Patients", patients.size.toString(), "Registered patients", Icons.Default.Healing, Blue, BlueLight, Modifier.weight(1f))
            StatCard("Appointments", appointments.size.toString(), "Hospital appointments", Icons.Default.Event, Red, RedLight, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Prescriptions", prescriptions.size.toString(), "Prescriptions recorded", Icons.Default.Medication, Purple, PurpleLight, Modifier.weight(1f))
            StatCard("Total Billing", Format.currency(Reports.revenue(invoices)), "Total billing amount", Icons.Default.Receipt, Green, GreenLight, Modifier.weight(1f))
        }

        SectionTitle("Quick Actions", "Quickly access important areas.")
        listOf(
            Quick("patient", "patients/new", "Patient Management", "Add and manage patients", Icons.Default.PersonAdd, Blue, BlueLight),
            Quick("appointments", "appointments/new", "Appointments", "Schedule appointments", Icons.Default.EventAvailable, Red, RedLight),
            Quick("prescriptions", "prescriptions/new", "Prescriptions", "Manage prescriptions", Icons.Default.Medication, Purple, PurpleLight),
            Quick("billing", "billing", "Billing System", "Manage hospital billing", Icons.Default.Payments, Green, GreenLight)
        ).filter { it.page in allowed }.forEach { q ->
            DcCard(onClick = { go(q.route) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBadge(q.icon, q.tint, q.bg)
                    Column(Modifier.weight(1f)) {
                        Text(q.title, fontWeight = FontWeight.Bold)
                        Text(q.sub, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }

        DcCard {
            Text("Appointment Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Hospital appointment statistics", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            BarList(Reports.appointmentsByStatus(appointments))
        }
    }
}

private data class Quick(
    val page: String,
    val route: String,
    val title: String,
    val sub: String,
    val icon: ImageVector,
    val tint: Color,
    val bg: Color
)
