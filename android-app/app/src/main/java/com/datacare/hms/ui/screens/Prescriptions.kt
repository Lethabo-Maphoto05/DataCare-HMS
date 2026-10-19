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
import com.datacare.hms.data.Prescription
import com.datacare.hms.ui.*
import com.datacare.hms.util.Validators

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionListScreen(vm: AppViewModel, onNew: () -> Unit) {
    val items by vm.prescriptions.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    // "My Prescriptions" = Active, "Prescription History" = Completed / Cancelled (Task 1 sitemap)
    var filter by rememberSaveable { mutableStateOf("All") }
    var pending by remember { mutableStateOf<Pair<Prescription, String>?>(null) }

    val filtered = items.filter { r ->
        val inFilter = when (filter) {
            "Active" -> r.status == "Active"
            "History" -> r.status != "Active"
            else -> true
        }
        inFilter && (query.isBlank() || listOf(r.prescriptionID, r.patientID, r.doctorID, r.medication)
            .any { it.contains(query.trim(), ignoreCase = true) })
    }.sortedByDescending { it.prescriptionDate }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            DcSearchBar(query, { query = it }, "Search prescriptions", Modifier.weight(1f))
            PrimaryButton("New", onNew, icon = Icons.Default.Add)
        }
        Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Active", "History").forEach { f ->
                FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(if (f == "Active") "My Prescriptions" else f) })
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (filtered.isEmpty()) item { EmptyState("No prescriptions found.", Icons.Default.Medication) }
            items(filtered) { r ->
                DcCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(r.medication, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(r.dosage, fontSize = 14.sp)
                        }
                        StatusChip(r.status)
                    }
                    Spacer(Modifier.height(6.dp))
                    InfoRow("Prescription", r.prescriptionID)
                    InfoRow("Patient", r.patientID)
                    InfoRow("Doctor", r.doctorID)
                    InfoRow("Date", r.prescriptionDate)
                    InfoRow("Notes", r.notes)
                    if (r.status == "Active") {
                        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SecondaryButton("Mark Completed", { pending = r to "Completed" }, Modifier.weight(1f), Icons.Default.CheckCircle)
                            SecondaryButton("Cancel", { pending = r to "Cancelled" }, Modifier.weight(1f), Icons.Default.Cancel)
                        }
                    }
                }
            }
        }
    }

    pending?.let { (r, status) ->
        ConfirmDialog(
            "Mark as $status?", "${r.medication} for ${r.patientID} will be marked $status.",
            "Yes, mark $status", { vm.setPrescriptionStatus(r, status) }, { pending = null }
        )
    }
}

@Composable
fun PrescriptionFormScreen(vm: AppViewModel, onDone: () -> Unit) {
    var patient by rememberSaveable { mutableStateOf("") }
    var doctor by rememberSaveable { mutableStateOf("") }
    var medication by rememberSaveable { mutableStateOf("") }
    var dosage by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(com.datacare.hms.util.Format.today()) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val busy by vm.busy.collectAsState()

    fun save() {
        val e = mutableMapOf<String, String>()
        Validators.requiredError("Patient ID", patient)?.let { e["patient"] = it }
        Validators.requiredError("Doctor", doctor)?.let { e["doctor"] = it }
        Validators.requiredError("Medication", medication)?.let { e["medication"] = it }
        Validators.requiredError("Dosage", dosage)?.let { e["dosage"] = it }
        if (!Validators.isValidDate(date)) e["date"] = "Choose a valid date"
        errors = e
        if (e.isEmpty()) {
            vm.savePrescription(
                mapOf(
                    "patientID" to patient.trim(), "doctorID" to doctor.trim(), "medication" to medication.trim(),
                    "dosage" to dosage.trim(), "notes" to notes.trim(), "prescriptionDate" to date
                )
            ) { ok -> if (ok) onDone() }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DcTextField("Patient ID", patient, { patient = it; errors = errors - "patient" }, icon = Icons.Default.Person, error = errors["patient"])
        DcTextField("Doctor", doctor, { doctor = it; errors = errors - "doctor" }, icon = Icons.Default.MedicalServices, error = errors["doctor"])
        DcTextField("Medication", medication, { medication = it; errors = errors - "medication" }, icon = Icons.Default.Medication, error = errors["medication"])
        DcTextField("Dosage (e.g. 500 mg twice daily)", dosage, { dosage = it; errors = errors - "dosage" }, error = errors["dosage"])
        DateField("Prescription Date", date, { date = it; errors = errors - "date" }, errors["date"])
        DcTextField("Notes (optional)", notes, { notes = it }, singleLine = false, minLines = 3)
        FormButtons("Save Prescription", busy, ::save, onDone)
    }
}
