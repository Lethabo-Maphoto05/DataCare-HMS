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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datacare.hms.AppViewModel
import com.datacare.hms.data.Patient
import com.datacare.hms.ui.*
import com.datacare.hms.ui.theme.*
import com.datacare.hms.util.Validators

@Composable
fun PatientListScreen(vm: AppViewModel, onAdd: () -> Unit) {
    val patients by vm.patients.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<Patient?>(null) }

    val filtered = patients.filter {
        query.isBlank() || listOf(it.patientID, it.firstName, it.lastName, it.phone).any { f -> f.contains(query.trim(), ignoreCase = true) }
    }.sortedBy { it.patientID }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            DcSearchBar(query, { query = it }, "Search patients", Modifier.weight(1f))
            PrimaryButton("Add", onAdd, icon = Icons.Default.Add)
        }
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (filtered.isEmpty()) item { EmptyState(if (patients.isEmpty()) "No patients yet. Tap Add to register one." else "No patients match your search.") }
            items(filtered) { p ->
                DcCard(onClick = { selected = p }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconBadge(Icons.Default.Person, Blue, BlueLight)
                        Column(Modifier.weight(1f)) {
                            Text(p.fullName(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${p.patientID}  |  ${p.gender}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(p.phone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
        }
    }

    selected?.let { p ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(p.fullName()) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    InfoRow("Patient ID", p.patientID)
                    InfoRow("Gender", p.gender)
                    InfoRow("Date of birth", p.dateOfBirth)
                    InfoRow("Phone", p.phone)
                    InfoRow("Address", p.address)
                    InfoRow("Medical history", p.medicalHistory)
                    InfoRow("Emergency contact", p.emergencyContact)
                }
            },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("Close") } }
        )
    }
}

@Composable
fun PatientFormScreen(vm: AppViewModel, onDone: () -> Unit) {
    var first by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("") }
    var dob by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var history by rememberSaveable { mutableStateOf("") }
    var emergency by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val busy by vm.busy.collectAsState()

    fun save() {
        val e = mutableMapOf<String, String>()
        Validators.requiredError("First name", first)?.let { e["first"] = it }
        Validators.requiredError("Last name", last)?.let { e["last"] = it }
        if (gender.isBlank()) e["gender"] = "Select a gender"
        if (dob.isBlank()) e["dob"] = "Date of birth is required"
        else if (!Validators.isValidDate(dob)) e["dob"] = "Use the date picker"
        else if (!Validators.isBefore(dob, com.datacare.hms.util.Format.today())) e["dob"] = "Date of birth must be in the past"
        Validators.phoneError(phone)?.let { e["phone"] = it }
        Validators.requiredError("Address", address)?.let { e["address"] = it }
        errors = e
        if (e.isEmpty()) {
            vm.savePatient(
                mapOf(
                    "firstName" to first.trim(), "lastName" to last.trim(), "gender" to gender, "dateOfBirth" to dob,
                    "phone" to phone.trim(), "address" to address.trim(), "medicalHistory" to history.trim(),
                    "emergencyContact" to emergency.trim()
                )
            ) { ok -> if (ok) onDone() }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DcTextField("First Name", first, { first = it; errors = errors - "first" }, Modifier.weight(1f), error = errors["first"])
            DcTextField("Last Name", last, { last = it; errors = errors - "last" }, Modifier.weight(1f), error = errors["last"])
        }
        DropdownField("Gender", gender, listOf("Male", "Female", "Other"), { gender = it; errors = errors - "gender" }, errors["gender"])
        DateField("Date of Birth", dob, { dob = it; errors = errors - "dob" }, errors["dob"])
        DcTextField("Phone Number", phone, { phone = it; errors = errors - "phone" }, icon = Icons.Default.Phone, error = errors["phone"], keyboardType = KeyboardType.Phone)
        DcTextField("Address", address, { address = it; errors = errors - "address" }, error = errors["address"], singleLine = false, minLines = 2)
        DcTextField("Medical History (optional)", history, { history = it }, singleLine = false, minLines = 3)
        DcTextField("Emergency Contact (optional)", emergency, { emergency = it })
        FormButtons("Save Patient", busy, ::save, onDone)
    }
}

/** Save / Cancel row used by every form (as in the wireframes). */
@Composable
fun FormButtons(saveLabel: String, busy: Boolean, onSave: () -> Unit, onCancel: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SecondaryButton("Cancel", onCancel, Modifier.weight(1f))
        PrimaryButton(if (busy) "Saving..." else saveLabel, onSave, Modifier.weight(1f), Icons.Default.Save, enabled = !busy)
    }
}
