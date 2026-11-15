package com.datacare.hms.data

/** Mirrors the current role navigation in web/main.js. */
object RoleNavigation {
    fun pagesFor(role: String): List<String> = when (role.lowercase()) {
        "admin" -> listOf("Dashboard", "Patients", "Appointments", "Prescriptions", "Billing", "Services", "Reports", "User Management")
        "doctor" -> listOf("Dashboard", "Services", "Appointments", "Prescriptions")
        "patient" -> listOf("Dashboard", "Services", "Appointments", "Prescriptions", "Billing")
        else -> listOf("Dashboard")
    }
}
