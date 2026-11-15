package com.datacare.hms.data

import com.google.gson.annotations.SerializedName

data class AppUser(
    @SerializedName("user_id") val userId: String = "",
    val email: String = "",
    @SerializedName("first_name") val firstName: String = "",
    @SerializedName("last_name") val lastName: String = "",
    @SerializedName("full_name") val fullName: String = "",
    val role: String = "",
    @SerializedName("patient_id") val patientId: Int? = null,
    @SerializedName("patient_number") val patientNumber: String? = null,
    @SerializedName("doctor_id") val doctorId: Int? = null,
    val specialisation: String? = null
)

data class Summary(
    val patients: Int = 0,
    val doctors: Int = 0,
    val appointments: Int = 0,
    val prescriptions: Int = 0,
    val revenue: Double = 0.0
)

data class PendingRegistration(
    @SerializedName("user_id") val userId: String = "",
    val email: String = "",
    @SerializedName("first_name") val firstName: String = "",
    @SerializedName("last_name") val lastName: String = "",
    @SerializedName("phone_number") val phoneNumber: String = "",
    @SerializedName("id_number") val idNumber: String = "",
    @SerializedName("approval_status") val approvalStatus: String = "Pending"
)

data class Patient(
    @SerializedName("patient_id") val patientId: Int = 0,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("patient_number") val patientNumber: String = "",
    @SerializedName("first_name") val firstName: String = "",
    @SerializedName("last_name") val lastName: String = "",
    @SerializedName("phone_number") val phoneNumber: String = "",
    @SerializedName("id_number") val idNumber: String? = null,
    val gender: String? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    val address: String? = null
) {
    val displayName: String get() = "$firstName $lastName".trim()
}

data class Doctor(
    @SerializedName("doctor_id") val doctorId: Int = 0,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("full_name") val fullName: String = "",
    val specialisation: String = "",
    @SerializedName("department_name") val departmentName: String? = null,
    @SerializedName("contact_number") val contactNumber: String? = null,
    val email: String? = null,
    val availability: String = "Available"
)

data class Appointment(
    @SerializedName("appointment_id") val appointmentId: Int = 0,
    @SerializedName("patient_id") val patientId: Int = 0,
    @SerializedName("doctor_id") val doctorId: Int = 0,
    @SerializedName("appointment_date") val appointmentDate: String = "",
    @SerializedName("appointment_time") val appointmentTime: String = "",
    @SerializedName("appointment_type") val appointmentType: String = "Consultation",
    val status: String = "Pending",
    @SerializedName("ward_allocation") val wardAllocation: String? = null,
    @SerializedName("patient_name") val patientName: String? = null,
    @SerializedName("patient_number") val patientNumber: String? = null,
    @SerializedName("doctor_name") val doctorName: String? = null,
    val specialisation: String? = null
)

data class PrescriptionItem(
    @SerializedName("medication_id") val medicationId: Int = 0,
    val medication: String = "",
    val strength: String? = null,
    val quantity: Int = 0,
    val dosage: String = "",
    @SerializedName("unit_price") val unitPrice: Double = 0.0
)

data class Prescription(
    @SerializedName("prescription_id") val prescriptionId: Int = 0,
    @SerializedName("patient_id") val patientId: Int = 0,
    @SerializedName("doctor_id") val doctorId: Int = 0,
    val notes: String? = null,
    @SerializedName("prescription_date") val prescriptionDate: String = "",
    @SerializedName("doctor_name") val doctorName: String? = null,
    val items: List<PrescriptionItem> = emptyList()
)

data class InvoiceLine(
    @SerializedName("item_id") val itemId: String? = null,
    val description: String = "",
    val quantity: Int = 0,
    @SerializedName("unit_price") val unitPrice: Double = 0.0,
    @SerializedName("line_total") val lineTotal: Double = 0.0
)

data class Invoice(
    @SerializedName("invoice_id") val invoiceId: Int = 0,
    @SerializedName("invoice_number") val invoiceNumber: String = "",
    @SerializedName("patient_id") val patientId: Int = 0,
    @SerializedName("prescription_id") val prescriptionId: Int? = null,
    @SerializedName("billing_date") val billingDate: String = "",
    @SerializedName("payment_status") val paymentStatus: String = "Unpaid",
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("delivery_required") val deliveryRequired: Boolean = false,
    val total: Double = 0.0,
    @SerializedName("patient_number") val patientNumber: String? = null,
    @SerializedName("patient_name") val patientName: String? = null,
    val lines: List<InvoiceLine> = emptyList()
)

data class ServiceItem(
    @SerializedName("service_id") val serviceId: Int = 0,
    val name: String = "",
    val description: String? = null,
    val price: Double = 0.0
)

data class Medication(
    @SerializedName("medication_id") val medicationId: Int = 0,
    val name: String = "",
    val strength: String? = null,
    @SerializedName("unit_price") val unitPrice: Double = 0.0,
    @SerializedName("stock_qty") val stockQty: Int = 0
)

data class Ward(
    @SerializedName("ward_id") val wardId: Int = 0,
    @SerializedName("ward_name") val wardName: String = "",
    @SerializedName("bed_number") val bedNumber: Int = 0,
    val status: String = "Available"
) {
    val label: String get() = "$wardName / Bed $bedNumber ($status)"
}

data class Admission(
    @SerializedName("admission_id") val admissionId: Int = 0,
    @SerializedName("patient_id") val patientId: Int = 0,
    @SerializedName("ward_id") val wardId: Int = 0,
    @SerializedName("admission_date") val admissionDate: String = "",
    val status: String = "Admitted"
)

data class NotificationItem(
    @SerializedName("notification_id") val notificationId: Int = 0,
    @SerializedName("user_id") val userId: String = "",
    val message: String = "",
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String = ""
)

data class RegisterResult(val message: String = "", val status: String = "Pending", @SerializedName("patient_number") val patientNumber: String? = null)
data class PrescriptionResult(@SerializedName("prescription_id") val prescriptionId: Int = 0, @SerializedName("invoice_id") val invoiceId: Int = 0)
data class PaymentResult(val message: String = "", @SerializedName("amount_paid") val amountPaid: Double = 0.0)
data class ApiMessage(val message: String = "")

data class WorkspaceState(
    val summary: Summary = Summary(),
    val registrations: List<PendingRegistration> = emptyList(),
    val patients: List<Patient> = emptyList(),
    val doctors: List<Doctor> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val prescriptions: List<Prescription> = emptyList(),
    val invoices: List<Invoice> = emptyList(),
    val services: List<ServiceItem> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val wards: List<Ward> = emptyList(),
    val notifications: List<NotificationItem> = emptyList(),
    val patientProfile: Patient? = null
)
