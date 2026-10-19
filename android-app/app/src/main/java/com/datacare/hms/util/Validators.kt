package com.datacare.hms.util

import java.text.SimpleDateFormat
import java.util.Locale

/** Pure input checks used by every form, so invalid input is rejected before it reaches Firebase. */
object Validators {
    private val EMAIL = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
    private val TIME = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")
    private val DATE_SHAPE = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    private val SA_PHONE = Regex("^(\\+27|0)\\d{9}$")

    const val MIN_PASSWORD = 6

    fun isValidEmail(email: String) = EMAIL.matches(email.trim())

    /** Returns an error message, or null when the password is acceptable. */
    fun passwordError(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        password.length < MIN_PASSWORD -> "Password must be at least $MIN_PASSWORD characters"
        password.length > 128 -> "Password is too long"
        else -> null
    }

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !isValidEmail(email) -> "Enter a valid email address"
        else -> null
    }

    fun requiredError(label: String, value: String): String? =
        if (value.isBlank()) "$label is required" else null

    fun isValidPhone(phone: String): Boolean =
        SA_PHONE.matches(phone.replace(" ", "").replace("-", ""))

    fun phoneError(phone: String): String? = when {
        phone.isBlank() -> "Phone number is required"
        !isValidPhone(phone) -> "Use a South African number, e.g. 082 123 4567"
        else -> null
    }

    fun isValidDate(date: String): Boolean {
        if (!DATE_SHAPE.matches(date)) return false
        return try {
            val f = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            f.isLenient = false
            f.parse(date) != null
        } catch (e: Exception) {
            false
        }
    }

    fun isValidTime(time: String) = TIME.matches(time)

    /** ISO dates compare correctly as strings. */
    fun isBefore(date: String, other: String) = date < other

    fun isValidMoney(value: String): Boolean {
        val v = value.trim().toDoubleOrNull() ?: return false
        return v.isFinite() && v >= 0.0 && v <= 10_000_000.0
    }

    fun moneyError(label: String, value: String, required: Boolean = false): String? = when {
        value.isBlank() -> if (required) "$label is required" else null
        !isValidMoney(value) -> "$label must be a number of 0 or more"
        else -> null
    }
}
