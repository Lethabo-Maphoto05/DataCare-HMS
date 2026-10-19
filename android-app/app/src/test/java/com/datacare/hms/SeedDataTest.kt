package com.datacare.hms

import com.datacare.hms.data.SeedData
import com.datacare.hms.util.Billing
import org.junit.Assert.*
import org.junit.Test

class SeedDataTest {
    @Test fun everyTableHasAtLeastTenRecords() {
        assertEquals(8, SeedData.all.size)
        SeedData.all.forEach { (table, docs) ->
            assertTrue("$table has only ${docs.size} records", docs.size >= 10)
        }
    }

    @Test fun documentIdsAreUniquePerTable() {
        SeedData.all.forEach { (table, docs) ->
            assertEquals("duplicate ids in $table", docs.size, docs.map { it.id }.toSet().size)
        }
    }

    @Test fun invoicesFollowTheBillingRules() {
        SeedData.invoices.forEach { d ->
            val consult = d.data["consultationFee"] as Double
            val meds = d.data["medicationCost"] as Double
            val outstanding = d.data["outstandingBalance"] as Double
            assertEquals(Billing.total(consult, meds), d.data["totalAmount"] as Double, 0.001)
            assertEquals(Billing.statusFor(outstanding), d.data["status"])
            assertTrue(outstanding <= d.data["totalAmount"] as Double)
        }
    }

    @Test fun appointmentsAndPrescriptionsPointAtSeededPatients() {
        val ids = SeedData.patients.map { it.data["patientID"] }.toSet()
        SeedData.appointments.forEach { assertTrue(it.data["patient"] in ids) }
        SeedData.prescriptions.forEach { assertTrue(it.data["patientID"] in ids) }
        SeedData.invoices.forEach { assertTrue(it.data["patientID"] in ids) }
    }

    @Test fun recordsUseTheSameFieldNamesAsTheWebApp() {
        val apt = SeedData.appointments.first().data.keys
        assertTrue(apt.containsAll(listOf("appointmentID", "patient", "doctor", "date", "time", "appointmentType", "wardAllocation", "status")))
        val rx = SeedData.prescriptions.first().data.keys
        assertTrue(rx.containsAll(listOf("prescriptionID", "patientID", "doctorID", "medication", "dosage", "notes", "prescriptionDate", "status")))
        val inv = SeedData.invoices.first().data.keys
        assertTrue(inv.containsAll(listOf("invoiceNumber", "patientID", "consultationFee", "medicationCost", "medicalAid", "paymentMethod", "outstandingBalance", "status")))
    }
}
