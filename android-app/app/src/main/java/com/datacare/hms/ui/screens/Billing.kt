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
import com.datacare.hms.data.Invoice
import com.datacare.hms.ui.*
import com.datacare.hms.ui.theme.*
import com.datacare.hms.util.Billing
import com.datacare.hms.util.Format
import com.datacare.hms.util.Reports
import com.datacare.hms.util.Validators

val PAYMENT_METHODS = listOf("Cash", "Card", "Medical Aid", "EFT")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingListScreen(vm: AppViewModel, onNew: () -> Unit) {
    val invoices by vm.invoices.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    // Views from the Task 1 sitemap: My Invoices, Medical Aid Claims, Payment History
    var filter by rememberSaveable { mutableStateOf("All") }
    var paying by remember { mutableStateOf<Invoice?>(null) }

    val filtered = invoices.filter { i ->
        val inFilter = when (filter) {
            "Outstanding" -> i.outstandingBalance > 0
            "Claims" -> i.medicalAid.isNotBlank()
            "Payments" -> i.outstandingBalance <= 0
            else -> true
        }
        inFilter && (query.isBlank() || listOf(i.invoiceNumber, i.patientID, i.medicalAid, i.paymentMethod)
            .any { it.contains(query.trim(), ignoreCase = true) })
    }.sortedByDescending { it.invoiceNumber }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Total Billing", Format.currency(Reports.revenue(invoices)), "All invoices", Icons.Default.Receipt, Green, GreenLight, Modifier.weight(1f))
            StatCard("Outstanding", Format.currency(Reports.outstanding(invoices)), "Still to be paid", Icons.Default.PendingActions, Amber, AmberLight, Modifier.weight(1f))
        }
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            DcSearchBar(query, { query = it }, "Search invoices", Modifier.weight(1f))
            PrimaryButton("New", onNew, icon = Icons.Default.Add)
        }
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("All" to "Invoices", "Outstanding" to "Due", "Claims" to "Claims", "Payments" to "Paid").forEach { (key, label) ->
                FilterChip(selected = filter == key, onClick = { filter = key }, label = { Text(label) })
            }
        }
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (filtered.isEmpty()) item { EmptyState("No invoices found.", Icons.Default.Receipt) }
            items(filtered) { i ->
                DcCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(i.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Patient: ${i.patientID}", fontSize = 14.sp)
                        }
                        StatusChip(i.status)
                    }
                    Spacer(Modifier.height(6.dp))
                    InfoRow("Consultation fee", Format.currency(i.consultationFee))
                    InfoRow("Medication cost", Format.currency(i.medicationCost))
                    InfoRow("Total", Format.currency(i.totalAmount))
                    InfoRow("Medical aid", i.medicalAid.ifBlank { "None" })
                    InfoRow("Payment method", i.paymentMethod)
                    InfoRow("Outstanding", Format.currency(i.outstandingBalance))
                    if (i.outstandingBalance > 0) {
                        PrimaryButton("Make Payment", { paying = i }, Modifier.fillMaxWidth().padding(top = 8.dp), Icons.Default.Payments)
                    }
                }
            }
        }
    }

    paying?.let { inv ->
        var amount by remember(inv.docId) { mutableStateOf(String.format(java.util.Locale.US, "%.2f", inv.outstandingBalance)) }
        var method by remember(inv.docId) { mutableStateOf(inv.paymentMethod.ifBlank { "Card" }) }
        var err by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { paying = null },
            title = { Text("Pay ${inv.invoiceNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Outstanding: ${Format.currency(inv.outstandingBalance)}")
                    DcTextField("Amount (R)", amount, { amount = it; err = null }, error = err, keyboardType = KeyboardType.Decimal)
                    DropdownField("Payment method", method, PAYMENT_METHODS, { method = it })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = amount.trim().toDoubleOrNull()
                    val problem = Billing.paymentError(value, inv.outstandingBalance)
                    if (problem != null) err = problem else { vm.makePayment(inv, value!!, method); paying = null }
                }) { Text("Pay") }
            },
            dismissButton = { TextButton(onClick = { paying = null }) { Text("Close") } }
        )
    }
}

@Composable
fun InvoiceFormScreen(vm: AppViewModel, onDone: () -> Unit) {
    var patient by rememberSaveable { mutableStateOf("") }
    var number by rememberSaveable { mutableStateOf(Format.newId("INV")) }
    var consult by rememberSaveable { mutableStateOf("") }
    var meds by rememberSaveable { mutableStateOf("") }
    var aid by rememberSaveable { mutableStateOf("") }
    var method by rememberSaveable { mutableStateOf("") }
    var outstanding by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val busy by vm.busy.collectAsState()

    val total = Billing.total(consult.toDoubleOrNull() ?: 0.0, meds.toDoubleOrNull() ?: 0.0)

    fun save() {
        val e = mutableMapOf<String, String>()
        Validators.requiredError("Patient ID", patient)?.let { e["patient"] = it }
        Validators.requiredError("Invoice number", number)?.let { e["number"] = it }
        Validators.moneyError("Consultation fee", consult, required = true)?.let { e["consult"] = it }
        Validators.moneyError("Medication cost", meds)?.let { e["meds"] = it }
        Validators.moneyError("Outstanding amount", outstanding)?.let { e["outstanding"] = it }
        if (method.isBlank()) e["method"] = "Select a payment method"
        if (e.isEmpty() && (outstanding.toDoubleOrNull() ?: 0.0) > total) e["outstanding"] = "Outstanding cannot be more than the total"
        errors = e
        if (e.isEmpty()) {
            vm.saveInvoice(
                patient.trim(), number.trim(), consult.toDouble(), meds.toDoubleOrNull() ?: 0.0,
                aid.trim(), method, outstanding.toDoubleOrNull() ?: 0.0
            ) { ok -> if (ok) onDone() }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DcTextField("Patient ID", patient, { patient = it; errors = errors - "patient" }, icon = Icons.Default.Person, error = errors["patient"])
        DcTextField("Invoice Number", number, { number = it; errors = errors - "number" }, error = errors["number"])
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DcTextField("Consultation Fee (R)", consult, { consult = it; errors = errors - "consult" }, Modifier.weight(1f), error = errors["consult"], keyboardType = KeyboardType.Decimal)
            DcTextField("Medication Cost (R)", meds, { meds = it; errors = errors - "meds" }, Modifier.weight(1f), error = errors["meds"], keyboardType = KeyboardType.Decimal)
        }
        DcCard { InfoRow("Total", Format.currency(total)) }
        DcTextField("Medical Aid (optional)", aid, { aid = it })
        DropdownField("Payment Method", method, PAYMENT_METHODS, { method = it; errors = errors - "method" }, errors["method"])
        DcTextField("Outstanding Amount (R)", outstanding, { outstanding = it; errors = errors - "outstanding" }, error = errors["outstanding"], keyboardType = KeyboardType.Decimal)
        FormButtons("Save Invoice", busy, ::save, onDone)
    }
}
