package com.datacare.hms

import com.datacare.hms.util.Format
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatTest {
    @Test fun greetingMatchesTimeOfDay() {
        assertEquals("Good morning", Format.greetingFor(0))
        assertEquals("Good morning", Format.greetingFor(11))
        assertEquals("Good afternoon", Format.greetingFor(12))
        assertEquals("Good afternoon", Format.greetingFor(16))
        assertEquals("Good evening", Format.greetingFor(17))
        assertEquals("Good evening", Format.greetingFor(23))
    }

    @Test fun currencyIsFormattedLikeTheWebApp() {
        assertEquals("R1,234.50", Format.currency(1234.5))
        assertEquals("R0.00", Format.currency(0.0))
    }

    @Test fun generatedIdsHaveThePrefixAndSixDigits() {
        val id = Format.newId("APT")
        assertTrue(Regex("^APT-\\d{6}$").matches(id))
    }
}
