package com.datacare.hms

import com.datacare.hms.util.Validators
import org.junit.Assert.*
import org.junit.Test

class ValidatorsTest {
    @Test fun validEmailsAreAccepted() {
        assertTrue(Validators.isValidEmail("nurse@datacare.co.za"))
        assertTrue(Validators.isValidEmail("  first.last+tag@mail.example.com "))
    }

    @Test fun invalidEmailsAreRejected() {
        listOf("", "plain", "a@b", "@x.com", "a b@x.com", "a@@x.com").forEach {
            assertFalse("'$it' should be invalid", Validators.isValidEmail(it))
        }
        assertEquals("Email is required", Validators.emailError("  "))
        assertEquals("Enter a valid email address", Validators.emailError("nope"))
        assertNull(Validators.emailError("ok@x.com"))
    }

    @Test fun passwordRules() {
        assertEquals("Password is required", Validators.passwordError(""))
        assertNotNull(Validators.passwordError("12345"))
        assertNull(Validators.passwordError("123456"))
        assertNotNull(Validators.passwordError("x".repeat(129)))
    }

    @Test fun southAfricanPhoneNumbers() {
        assertTrue(Validators.isValidPhone("082 123 4567"))
        assertTrue(Validators.isValidPhone("0821234567"))
        assertTrue(Validators.isValidPhone("+27 82 123 4567"))
        assertFalse(Validators.isValidPhone("12345"))
        assertFalse(Validators.isValidPhone("abcdefghij"))
        assertNotNull(Validators.phoneError(""))
    }

    @Test fun dates() {
        assertTrue(Validators.isValidDate("2026-10-07"))
        assertFalse(Validators.isValidDate("2026-02-30"))
        assertFalse(Validators.isValidDate("2026-13-01"))
        assertFalse(Validators.isValidDate("07/10/2026"))
        assertFalse(Validators.isValidDate(""))
        assertTrue(Validators.isBefore("2020-01-01", "2026-10-07"))
        assertFalse(Validators.isBefore("2026-10-07", "2026-10-07"))
    }

    @Test fun times() {
        assertTrue(Validators.isValidTime("00:00"))
        assertTrue(Validators.isValidTime("23:59"))
        assertFalse(Validators.isValidTime("24:00"))
        assertFalse(Validators.isValidTime("9:30"))
        assertFalse(Validators.isValidTime("12:60"))
    }

    @Test fun money() {
        assertTrue(Validators.isValidMoney("0"))
        assertTrue(Validators.isValidMoney("450.50"))
        assertFalse(Validators.isValidMoney("-1"))
        assertFalse(Validators.isValidMoney("abc"))
        assertFalse(Validators.isValidMoney("NaN"))
        assertNull(Validators.moneyError("Fee", "", required = false))
        assertEquals("Fee is required", Validators.moneyError("Fee", "", required = true))
        assertNotNull(Validators.moneyError("Fee", "-5"))
    }

    @Test fun requiredFields() {
        assertNull(Validators.requiredError("Name", "Thabo"))
        assertEquals("Name is required", Validators.requiredError("Name", "   "))
    }
}
