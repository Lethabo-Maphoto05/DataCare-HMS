package com.datacare.hms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datacare.hms.AppViewModel
import com.datacare.hms.ui.*
import com.datacare.hms.ui.theme.*

private data class Service(val name: String, val match: String, val text: String, val icon: ImageVector, val tint: Color, val bg: Color)

// Same six services (and wording) as pages/services.html on the website.
private val SERVICES = listOf(
    Service("Emergency", "Emergency", "Immediate medical assistance and emergency care for patients requiring urgent attention.", Icons.Default.Emergency, Red, RedLight),
    Service("Surgery", "Surgery", "Surgical procedures performed by qualified medical professionals.", Icons.Default.MedicalServices, Blue, BlueLight),
    Service("Maternity", "Maternity", "Pregnancy, childbirth and postnatal healthcare services.", Icons.Default.ChildFriendly, Purple, PurpleLight),
    Service("Orthopaedics", "Orthopaedics", "Diagnosis and treatment of conditions affecting bones, joints and muscles.", Icons.AutoMirrored.Filled.Accessible, Green, GreenLight),
    Service("Cardiology", "Cardiology", "Heart and cardiovascular healthcare, diagnosis and treatment.", Icons.Default.Favorite, Red, RedLight),
    Service("Radiology", "Radiology", "Medical imaging and diagnostic services to assist healthcare professionals.", Icons.Default.Radar, Blue, BlueLight)
)

@Composable
fun ServicesScreen(vm: AppViewModel) {
    val departments by vm.departments.collectAsState()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionTitle("Hospital Services", "DataCare provides a range of healthcare services for patients and the community.")
        }
        items(SERVICES) { s ->
            val dept = departments.firstOrNull { it.name.contains(s.match, ignoreCase = true) }
            DcCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBadge(s.icon, s.tint, s.bg, 48)
                    Text(s.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text(s.text, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (dept != null) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    InfoRow("Head of department", dept.head)
                    InfoRow("Location", dept.location)
                    InfoRow("Phone", dept.phone)
                }
            }
        }
    }
}