package com.datacare.hms

import com.datacare.hms.util.Billing
import org.junit.Assert.*
import org.junit.Test

class BillingTest {
    @Test fun totalIsConsultationPlusMedication() {
        assertEquals(650.0, Billing.total(500.0, 150.0), 0.001)
        assertEquals(0.30, Billing.total(0.1, 0.2), 0.0001) // floating point safe
    }

    @Test fun statusMatchesWebRule() {
        assertEquals("Paid", Billing.statusFor(0.0))
        assertEquals("Outstanding", Billing.statusFor(0.01))
    }

    @Test fun paymentReducesBalanceButNeverBelowZero() {
        assertEquals(100.0, Billing.balanceAfterPayment(250.0, 150.0), 0.001)
        assertEquals(0.0, Billing.balanceAfterPayment(100.0, 100.0), 0.001)
        assertEquals(0.0, Billing.balanceAfterPayment(100.0, 500.0), 0.001)
    }

    @Test fun paymentValidation() {
        assertNotNull(Billing.paymentError(null, 100.0))
        assertNotNull(Billing.paymentError(0.0, 100.0))
        assertNotNull(Billing.paymentError(-5.0, 100.0))
        assertNotNull(Billing.paymentError(150.0, 100.0))
        assertNull(Billing.paymentError(100.0, 100.0))
        assertNull(Billing.paymentError(40.0, 100.0))
    }
}
