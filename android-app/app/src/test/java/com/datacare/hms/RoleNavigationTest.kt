package com.datacare.hms

import com.datacare.hms.data.RoleNavigation
import org.junit.Assert.assertEquals
import org.junit.Test

class RoleNavigationTest {
    @Test fun adminNavigationMatchesWebApp() {
        assertEquals(listOf("Dashboard", "Patients", "Appointments", "Prescriptions", "Billing", "Services", "Reports", "User Management"), RoleNavigation.pagesFor("admin"))
    }

    @Test fun doctorNavigationMatchesWebApp() {
        assertEquals(listOf("Dashboard", "Services", "Appointments", "Prescriptions"), RoleNavigation.pagesFor("doctor"))
    }

    @Test fun patientNavigationMatchesWebApp() {
        assertEquals(listOf("Dashboard", "Services", "Appointments", "Prescriptions", "Billing"), RoleNavigation.pagesFor("patient"))
    }

    @Test fun unknownRoleHasNoExtraPages() {
        assertEquals(listOf("Dashboard"), RoleNavigation.pagesFor("unknown"))
    }
}
