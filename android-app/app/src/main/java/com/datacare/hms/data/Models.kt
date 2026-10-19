package com.datacare.hms.data

import com.google.firebase.firestore.DocumentId

/*
 * Field names match the documents written by the DataCare web app (js/main.js),
 * so records created on either platform appear on both.
 * Every property has a default value because Firestore needs a no-argument constructor.
 */

data class UserProfile(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val role: String = "Patient",
    val createdAt: String = ""
) {
    fun fullName(): String = "$firstName $lastName".trim().ifBlank { email }
}

data class Patient(
    @DocumentId val docId: String = "",
    val patientID: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val phone: String = "",
    val address: String = "",
    val medicalHistory: String = "",
    val emergencyContact: String = "",
    val createdBy: String = "",
    val createdAt: String = ""
) {
    fun fullName(): String = "$firstName $lastName".trim()
}

data class Appointment(
    @DocumentId val docId: String = "",
    val appointmentID: String = "",
    val patient: String = "",
    val doctor: String = "",
    val date: String = "",
    val time: String = "",
    val appointmentType: String = "",
    val wardAllocation: String = "",
    val status: String = "Confirmed",
    val createdBy: String = "",
    val createdAt: String = ""
)

data class Prescription(
    @DocumentId val docId: String = "",
    val prescriptionID: String = "",
    val patientID: String = "",
    val doctorID: String = "",
    val medication: String = "",
    val dosage: String = "",
    val notes: String = "",
    val prescriptionDate: String = "",
    val status: String = "Active",
    val createdBy: String = "",
    val createdAt: String = ""
)

data class Invoice(
    @DocumentId val docId: String = "",
    val invoiceNumber: String = "",
    val patientID: String = "",
    val consultationFee: Double = 0.0,
    val medicationCost: Double = 0.0,
    val totalAmount: Double = 0.0,
    val medicalAid: String = "",
    val paymentMethod: String = "",
    val outstandingBalance: Double = 0.0,
    val status: String = "Paid",
    val createdBy: String = "",
    val createdAt: String = ""
)

// ---- Extra ERD entities from the Task 1 design (Department, Staff, Inventory, MedicalRecord) ----

data class Department(
    @DocumentId val docId: String = "",
    val departmentID: String = "",
    val name: String = "",
    val head: String = "",
    val location: String = "",
    val phone: String = "",
    val description: String = ""
)

data class Staff(
    @DocumentId val docId: String = "",
    val staffID: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: String = "",
    val department: String = "",
    val email: String = "",
    val phone: String = ""
) {
    fun fullName(): String = "$firstName $lastName".trim()
}

data class InventoryItem(
    @DocumentId val docId: String = "",
    val itemID: String = "",
    val itemName: String = "",
    val category: String = "",
    val quantity: Long = 0,
    val reorderLevel: Long = 0,
    val unitCost: Double = 0.0,
    val supplier: String = ""
)

data class MedicalRecord(
    @DocumentId val docId: String = "",
    val recordID: String = "",
    val patientID: String = "",
    val diagnosis: String = "",
    val treatment: String = "",
    val doctor: String = "",
    val recordDate: String = "",
    val notes: String = ""
)

/** Collection names used in Firestore (same as the web app). */
object Collections {
    const val USERS = "users"
    const val PATIENTS = "patients"
    const val APPOINTMENTS = "appointments"
    const val PRESCRIPTIONS = "prescriptions"
    const val BILLING = "billing"
    const val DEPARTMENTS = "departments"
    const val STAFF = "staff"
    const val INVENTORY = "inventory"
    const val MEDICAL_RECORDS = "medicalRecords"
}
