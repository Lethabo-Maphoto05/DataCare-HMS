package com.datacare.hms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datacare.hms.AppViewModel
import com.datacare.hms.data.Appointment
import com.datacare.hms.ui.*
import com.datacare.hms.util.Format
import com.datacare.hms.util.Validators

val APPOINTMENT_TYPES = listOf("Consultation", "Follow-up", "Emergency", "Specialist", "Check-up")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentListScreen(vm: AppViewModel, onNew: () -> Unit) {
    val appointments by vm.appointments.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    var rescheduling by remember { mutableStateOf<Appointment?>(null) }
    var cancelling by remember { mutableStateOf<Appointment?>(null) }

    val filtered = appointments.filter { a ->
        (filter == "All" || a.status == filter) &&
            (query.isBlank() || listOf(a.appointmentID, a.patient, a.doctor, a.appointmentType, a.wardAllocation)
                .any { it.contains(query.trim(), ignoreCase = true) })
    }.sortedWith(compareBy({ it.date }, { it.time }))

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            DcSearchBar(query, { query = it }, "Search appointments", Modifier.weight(1f))
            PrimaryButton("New", onNew, icon = Icons.Default.Add)
        }
        Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Confirmed", "Completed", "Cancelled").forEach { f ->
                FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f) })
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (filtered.isEmpty()) item { EmptyState("No appointments found.", Icons.Default.EventBusy) }
            items(filtered) { a ->
                DcCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(a.appointmentID, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Patient: ${a.patient}", fontSize = 14.sp)
                        }
                        StatusChip(a.status)
                    }
                    Spacer(Modifier.height(6.dp))
                    InfoRow("Doctor", a.doctor)
                    InfoRow("Date & time", "${a.date}  ${a.time}")
                    InfoRow("Type", a.appointmentType)
                    InfoRow("Ward", a.wardAllocation)
                    if (a.status == "Confirmed" || a.status == "Pending") {
                        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SecondaryButton("Reschedule", { rescheduling = a }, Modifier.weight(1f), Icons.Default.EditCalendar)
                            SecondaryButton("Cancel", { cancelling = a }, Modifier.weight(1f), Icons.Default.Cancel)
                        }
                    }
                }
            }
        }
    }

    rescheduling?.let { a ->
        var date by remember(a.docId) { mutableStateOf(a.date) }
        var time by remember(a.docId) { mutableStateOf(a.time) }
        var err by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { rescheduling = null },
            title = { Text("Reschedule ${a.appointmentID}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DateField("New date", date, { date = it; err = null })
                    TimeField("New time", time, { time = it; err = null }, err)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (!Validators.isValidDate(date) || !Validators.isValidTime(time)) err = "Choose a valid date and time"
                    else if (Validators.isBefore(date, Format.today())) err = "The date cannot be in the past"
                    else { vm.rescheduleAppointment(a, date, time); rescheduling = null }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { rescheduling = null }) { Text("Close") } }
        )
    }

    cancelling?.let { a ->
        ConfirmDialog(
            "Cancel appointment?", "${a.appointmentID} for ${a.patient} on ${a.date} will be cancelled.",
            "Cancel appointment", { vm.setAppointmentStatus(a, "Cancelled") }, { cancelling = null }
        )
    }
}

@Composable
fun AppointmentFormScreen(vm: AppViewModel, onDone: () -> Unit) {
    var patient by rememberSaveable { mutableStateOf("") }
    var doctor by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("") }
    var ward by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val busy by vm.busy.collectAsState()

    fun save() {
        val e = mutableMapOf<String, String>()
        Validators.requiredError("Patient ID or name", patient)?.let { e["patient"] = it }
        Validators.requiredError("Doctor", doctor)?.let { e["doctor"] = it }
        if (date.isBlank()) e["date"] = "Date is required"
        else if (!Validators.isValidDate(date)) e["date"] = "Use the date picker"
        else if (Validators.isBefore(date, Format.today())) e["date"] = "The date cannot be in the past"
        if (!Validators.isValidTime(time)) e["time"] = "Time is required"
        if (type.isBlank()) e["type"] = "Select an appointment type"
        errors = e
        if (e.isEmpty()) {
            vm.saveAppointment(
                mapOf(
                    "patient" to patient.trim(), "doctor" to doctor.trim(), "date" to date, "time" to time,
                    "appointmentType" to type, "wardAllocation" to ward.trim()
                )
            ) { ok -> if (ok) onDone() }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DcTextField("Patient ID / Name", patient, { patient = it; errors = errors - "patient" }, icon = Icons.Default.Person, error = errors["patient"])
        DcTextField("Doctor", doctor, { doctor = it; errors = errors - "doctor" }, icon = Icons.Default.MedicalServices, error = errors["doctor"])
        DateField("Date", date, { date = it; errors = errors - "date" }, errors["date"])
        TimeField("Time", time, { time = it; errors = errors - "time" }, errors["time"])
        DropdownField("Appointment Type", type, APPOINTMENT_TYPES, { type = it; errors = errors - "type" }, errors["type"])
        DcTextField("Ward Allocation (e.g. Ward A)", ward, { ward = it }, icon = Icons.Default.Bed)
        FormButtons("Save", busy, ::save, onDone)
    }
}
