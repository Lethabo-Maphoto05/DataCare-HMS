package com.datacare.hms.util

import com.datacare.hms.data.Appointment
import com.datacare.hms.data.Invoice
import com.datacare.hms.data.Patient

/** Report maths kept free of Android classes so it can be unit-tested. */
object Reports {
    fun countBy(items: List<String>): List<Pair<String, Int>> =
        items.filter { it.isNotBlank() }.groupingBy { it }.eachCount()
            .toList().sortedByDescending { it.second }

    fun revenue(invoices: List<Invoice>): Double = Billing.round2(invoices.sumOf { it.totalAmount })
    fun outstanding(invoices: List<Invoice>): Double = Billing.round2(invoices.sumOf { it.outstandingBalance })
    fun collected(invoices: List<Invoice>): Double = Billing.round2(revenue(invoices) - outstanding(invoices))

    fun appointmentsByStatus(a: List<Appointment>) = countBy(a.map { it.status })
    fun appointmentsByType(a: List<Appointment>) = countBy(a.map { it.appointmentType })
    fun appointmentsByWard(a: List<Appointment>) = countBy(a.map { it.wardAllocation })
    fun patientsByGender(p: List<Patient>) = countBy(p.map { it.gender })
    fun invoicesByPayment(i: List<Invoice>) = countBy(i.map { it.paymentMethod })
}
