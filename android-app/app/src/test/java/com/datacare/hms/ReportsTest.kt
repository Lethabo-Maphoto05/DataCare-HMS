package com.datacare.hms

import com.datacare.hms.data.Appointment
import com.datacare.hms.data.Invoice
import com.datacare.hms.util.Reports
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportsTest {
    private val invoices = listOf(
        Invoice(totalAmount = 600.0, outstandingBalance = 0.0),
        Invoice(totalAmount = 400.0, outstandingBalance = 150.0)
    )

    @Test fun revenueOutstandingAndCollected() {
        assertEquals(1000.0, Reports.revenue(invoices), 0.001)
        assertEquals(150.0, Reports.outstanding(invoices), 0.001)
        assertEquals(850.0, Reports.collected(invoices), 0.001)
    }

    @Test fun countsAreSortedHighestFirstAndBlanksIgnored() {
        val appts = listOf("Confirmed", "Completed", "Confirmed", "", "Cancelled", "Confirmed").map { Appointment(status = it) }
        val result = Reports.appointmentsByStatus(appts)
        assertEquals("Confirmed" to 3, result.first())
        assertEquals(3, result.size)
        assertTrue(result.none { it.first.isBlank() })
    }

    @Test fun emptyDataGivesEmptyReports() {
        assertEquals(0.0, Reports.revenue(emptyList()), 0.0)
        assertTrue(Reports.appointmentsByStatus(emptyList()).isEmpty())
    }
}
