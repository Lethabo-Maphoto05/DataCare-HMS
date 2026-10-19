package com.datacare.hms.data

/** Role-based access. Mirrors `rolePermissions` in the web app's js/main.js exactly. */
object Roles {
    val all = listOf(
        "Patient", "Receptionist", "Doctor", "Nurse",
        "Pharmacist", "Billing Clerk", "Hospital Manager", "System Administrator"
    )

    const val ADMIN = "System Administrator"

    private val permissions: Map<String, Set<String>> = mapOf(
        "Patient" to setOf("dashboard", "services", "appointments", "prescriptions", "billing"),
        "Receptionist" to setOf("dashboard", "services", "patient", "appointments"),
        "Doctor" to setOf("dashboard", "patient", "appointments", "prescriptions", "reports"),
        "Nurse" to setOf("dashboard", "patient", "appointments"),
        "Pharmacist" to setOf("dashboard", "prescriptions"),
        "Billing Clerk" to setOf("dashboard", "billing", "reports"),
        "Hospital Manager" to setOf(
            "dashboard", "services", "patient", "appointments", "prescriptions", "billing", "reports"
        ),
        "System Administrator" to setOf(
            "dashboard", "services", "patient", "appointments", "prescriptions", "billing", "reports", "users"
        )
    )

    fun pagesFor(role: String): Set<String> = permissions[role] ?: setOf("dashboard")

    fun canAccess(role: String, page: String): Boolean = page in pagesFor(role)
}
