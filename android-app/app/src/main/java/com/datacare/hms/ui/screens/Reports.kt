package com.datacare.hms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.datacare.hms.AppViewModel
import com.datacare.hms.ui.*
import com.datacare.hms.ui.theme.*
import com.datacare.hms.util.Format
import com.datacare.hms.util.Reports

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(vm: AppViewModel) {
    val patients by vm.patients.collectAsState()
    val appointments by vm.appointments.collectAsState()
    val prescriptions by vm.prescriptions.collectAsState()
    val invoices by vm.invoices.collectAsState()
    var view by rememberSaveable { mutableStateOf("Patient Statistics") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Patients", patients.size.toString(), "Registered", Icons.Default.Healing, Blue, BlueLight, Modifier.weight(1f))
            StatCard("Appointments", appointments.size.toString(), "Booked", Icons.Default.Event, Red, RedLight, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Prescriptions", prescriptions.size.toString(), "Recorded", Icons.Default.Medication, Purple, PurpleLight, Modifier.weight(1f))
            StatCard("Revenue", Format.currency(Reports.revenue(invoices)), "Total billed", Icons.Default.Receipt, Green, GreenLight, Modifier.weight(1f))
        }

        SectionTitle("Reports", "Choose a report to view.")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            listOf("Patient Statistics", "Operational", "Financial").forEach { name ->
                FilterChip(selected = view == name, onClick = { view = name }, label = { Text(name) })
            }
        }

        when (view) {
            "Patient Statistics" -> {
                ReportCard("Patients by gender", "Total registered: ${patients.size}") {
                    BarList(Reports.patientsByGender(patients), Blue)
                }
                ReportCard("Prescriptions by status", "Total: ${prescriptions.size}") {
                    BarList(Reports.countBy(prescriptions.map { it.status }), Purple)
                }
            }
            "Operational" -> {
                ReportCard("Appointments by status", "Total: ${appointments.size}") {
                    BarList(Reports.appointmentsByStatus(appointments), Red)
                }
                ReportCard("Appointments by type", null) { BarList(Reports.appointmentsByType(appointments), Blue) }
                ReportCard("Ward allocation", null) { BarList(Reports.appointmentsByWard(appointments), Green) }
            }
            else -> {
                ReportCard("Revenue summary", null) {
                    InfoRow("Total billed", Format.currency(Reports.revenue(invoices)))
                    InfoRow("Collected", Format.currency(Reports.collected(invoices)))
                    InfoRow("Outstanding", Format.currency(Reports.outstanding(invoices)))
                    InfoRow("Invoices", invoices.size.toString())
                }
                ReportCard("Billed vs outstanding", null) {
                    BarList(
                        listOf("Collected" to Reports.collected(invoices), "Outstanding" to Reports.outstanding(invoices)),
                        Green
                    ) { Format.currency(it.toDouble()) }
                }
                ReportCard("Invoices by payment method", null) { BarList(Reports.invoicesByPayment(invoices), Blue) }
            }
        }
    }
}

@Composable
private fun ReportCard(title: String, subtitle: String?, content: @Composable ColumnScope.() -> Unit) {
    DcCard {
        SectionTitle(title, subtitle)
        Spacer(Modifier.height(6.dp))
        content()
    }
}
