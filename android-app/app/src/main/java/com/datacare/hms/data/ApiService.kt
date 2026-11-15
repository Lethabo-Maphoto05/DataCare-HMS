package com.datacare.hms.data

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("auth/me") suspend fun me(@Header("Authorization") auth: String): Response<AppUser>
    @POST("auth/register") suspend fun register(@Header("Authorization") auth: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<RegisterResult>

    @GET("reports/summary") suspend fun summary(@Header("Authorization") auth: String): Response<Summary>
    @GET("admin/registrations") suspend fun registrations(@Header("Authorization") auth: String): Response<List<PendingRegistration>>
    @PATCH("admin/registrations/{id}") suspend fun updateRegistration(@Header("Authorization") auth: String, @Path("id") id: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<JsonObject>
    @GET("admin/users") suspend fun users(@Header("Authorization") auth: String): Response<List<AppUser>>
    @POST("admin/doctors") suspend fun createDoctor(@Header("Authorization") auth: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<JsonObject>
    @GET("doctors") suspend fun doctors(@Header("Authorization") auth: String): Response<List<Doctor>>
    @GET("patients") suspend fun patients(@Header("Authorization") auth: String): Response<List<Patient>>
    @GET("patients/me") suspend fun myPatientProfile(@Header("Authorization") auth: String): Response<Patient>
    @POST("admin/patients") suspend fun createPatient(@Header("Authorization") auth: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<Patient>
    @GET("admin/wards") suspend fun wards(@Header("Authorization") auth: String): Response<List<Ward>>
    @POST("admin/wards") suspend fun createWard(@Header("Authorization") auth: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<Ward>
    @POST("admin/admissions") suspend fun admitPatient(@Header("Authorization") auth: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<Admission>
    @POST("admin/appointments") suspend fun bookAdminAppointment(@Header("Authorization") auth: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<Appointment>

    @GET("services") suspend fun services(@Header("Authorization") auth: String): Response<List<ServiceItem>>
    @GET("medications") suspend fun medications(@Header("Authorization") auth: String): Response<List<Medication>>
    @GET("appointments/doctor") suspend fun doctorAppointments(@Header("Authorization") auth: String): Response<List<Appointment>>
    @GET("appointments/mine") suspend fun myAppointments(@Header("Authorization") auth: String): Response<List<Appointment>>
    @POST("appointments") suspend fun requestAppointment(@Header("Authorization") auth: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<Appointment>
    @PATCH("appointments/{id}/confirm") suspend fun confirmAppointment(@Header("Authorization") auth: String, @Path("id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<Appointment>
    @PATCH("appointments/{id}/status") suspend fun updateAppointmentStatus(@Header("Authorization") auth: String, @Path("id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<Appointment>
    @POST("prescriptions") suspend fun createPrescription(@Header("Authorization") auth: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<PrescriptionResult>
    @GET("prescriptions/mine") suspend fun myPrescriptions(@Header("Authorization") auth: String): Response<List<Prescription>>

    @GET("admin/invoices") suspend fun adminInvoices(@Header("Authorization") auth: String): Response<List<Invoice>>
    @GET("invoices/mine") suspend fun myInvoices(@Header("Authorization") auth: String): Response<List<Invoice>>
    @GET("invoices/{id}") suspend fun invoice(@Header("Authorization") auth: String, @Path("id") id: Int): Response<Invoice>
    @POST("invoices/{id}/pay") suspend fun payInvoice(@Header("Authorization") auth: String, @Path("id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<PaymentResult>

    @GET("notifications") suspend fun notifications(@Header("Authorization") auth: String): Response<List<NotificationItem>>
    @PATCH("notifications/{id}/read") suspend fun markNotificationRead(@Header("Authorization") auth: String, @Path("id") id: Int): Response<JsonObject>
}
