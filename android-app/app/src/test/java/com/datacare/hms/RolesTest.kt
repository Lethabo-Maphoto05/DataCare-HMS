package com.datacare.hms

import com.datacare.hms.data.Roles
import org.junit.Assert.*
import org.junit.Test

class RolesTest {
    @Test fun eightRolesLikeTheWebApp() {
        assertEquals(8, Roles.all.size)
        assertTrue("Billing Clerk" in Roles.all)
    }

    @Test fun patientsCannotSeeStaffAreas() {
        assertTrue(Roles.canAccess("Patient", "appointments"))
        assertFalse(Roles.canAccess("Patient", "patient"))
        assertFalse(Roles.canAccess("Patient", "reports"))
    }

    @Test fun pharmacistOnlySeesPrescriptions() {
        assertEquals(setOf("dashboard", "prescriptions"), Roles.pagesFor("Pharmacist"))
    }

    @Test fun onlyAdministratorManagesUsers() {
        Roles.all.forEach { role ->
            assertEquals(role == "System Administrator", Roles.canAccess(role, "users"))
        }
    }

    @Test fun everyRoleCanOpenDashboardAndUnknownRoleFallsBack() {
        Roles.all.forEach { assertTrue(Roles.canAccess(it, "dashboard")) }
        assertEquals(setOf("dashboard"), Roles.pagesFor("Unknown"))
    }

    @Test fun managerSeesEverythingExceptUserManagement() {
        assertEquals(
            setOf("dashboard", "services", "patient", "appointments", "prescriptions", "billing", "reports"),
            Roles.pagesFor("Hospital Manager")
        )
    }
}
