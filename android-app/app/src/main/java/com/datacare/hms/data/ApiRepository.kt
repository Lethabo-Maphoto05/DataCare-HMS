package com.datacare.hms.data

import com.datacare.hms.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class ApiException(val status: Int, override val message: String) : Exception(message)

class ApiRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    private val api: ApiService by lazy {
        val gson = GsonBuilder().create()
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    private suspend fun token(): String {
        val user = auth.currentUser ?: throw ApiException(401, "Please sign in again.")
        val value = user.getIdToken(true).await().token
        return "Bearer ${value ?: throw ApiException(401, "Could not refresh your login token.") }"
    }

    private fun errorMessage(response: Response<*>): String {
        val raw = response.errorBody()?.string().orEmpty()
        return try {
            val obj = JSONObject(raw)
            val direct = obj.optString("error")
            if (direct.isNotBlank()) direct else {
                val errors = obj.optJSONArray("errors")
                if (errors != null) (0 until errors.length()).joinToString(" ") { errors.optString(it) }
                else "Request failed (${response.code()}). Please try again."
            }
        } catch (_: Exception) {
            "Request failed (${response.code()}). Please try again."
        }
    }

    private suspend fun <T : Any> call(block: suspend (String) -> Response<T>): T {
        val response = block(token())
        if (!response.isSuccessful) throw ApiException(response.code(), errorMessage(response))
        return response.body() ?: throw ApiException(response.code(), "The server returned an empty response.")
    }

    suspend fun me() = call { api.me(it) }
    suspend fun register(first: String, last: String, phone: String, idNumber: String, email: String, password: String) = call {
        api.register(it, mapOf("first_name" to first, "last_name" to last, "phone_number" to phone, "id_number" to idNumber, "email" to email, "password" to password))
    }
    suspend fun summary() = call { api.summary(it) }
    suspend fun registrations() = call { api.registrations(it) }
    suspend fun updateRegistration(id: String, status: String) = call { api.updateRegistration(it, id, mapOf("status" to status)) }
    suspend fun createDoctor(first: String, last: String, email: String, phone: String, specialisation: String, department: String, password: String) = call {
        api.createDoctor(it, mapOf("first_name" to first, "last_name" to last, "email" to email, "phone_number" to phone, "specialisation" to specialisation, "department_name" to department, "temporary_password" to password))
    }
    suspend fun doctors() = call { api.doctors(it) }
    suspend fun patients() = call { api.patients(it) }
    suspend fun myPatientProfile() = call { api.myPatientProfile(it) }
    suspend fun createPatient(first: String, last: String, phone: String, idNumber: String) = call {
        api.createPatient(it, mapOf("first_name" to first, "last_name" to last, "phone_number" to phone, "id_number" to idNumber))
    }
    suspend fun wards() = call { api.wards(it) }
    suspend fun createWard(name: String, bed: Int) = call { api.createWard(it, mapOf("ward_name" to name, "bed_number" to bed)) }
    suspend fun admit(patientId: Int, wardId: Int, doctorId: Int?, date: String, notes: String) = call {
        api.admitPatient(it, mapOf("patient_id" to patientId, "ward_id" to wardId, "doctor_id" to (doctorId ?: 0), "admission_date" to date, "notes" to notes))
    }
    suspend fun bookAdminAppointment(patientId: Int, doctorId: Int, date: String, time: String) = call {
        api.bookAdminAppointment(it, mapOf("patient_id" to patientId, "doctor_id" to doctorId, "appointment_date" to date, "appointment_time" to time))
    }
    suspend fun services() = call { api.services(it) }
    suspend fun medications() = call { api.medications(it) }
    suspend fun doctorAppointments() = call { api.doctorAppointments(it) }
    suspend fun myAppointments() = call { api.myAppointments(it) }
    suspend fun requestAppointment(doctorId: Int, date: String, time: String) = call {
        api.requestAppointment(it, mapOf("doctor_id" to doctorId, "appointment_date" to date, "appointment_time" to time))
    }
    suspend fun confirmAppointment(id: Int, date: String, time: String, ward: String) = call {
        api.confirmAppointment(it, id, mapOf("appointment_date" to date, "appointment_time" to time, "ward_allocation" to ward))
    }
    suspend fun appointmentStatus(id: Int, status: String) = call { api.updateAppointmentStatus(it, id, mapOf("status" to status)) }
    suspend fun createPrescription(patientId: Int, doctorId: Int?, serviceId: Int, medicationId: Int, quantity: Int, dosage: String, notes: String) = call {
        val body = mutableMapOf<String, Any>(
            "patient_id" to patientId, "service_id" to serviceId,
            "items" to listOf(mapOf("medication_id" to medicationId, "quantity" to quantity, "dosage" to dosage))
        )
        if (doctorId != null) body["doctor_id"] = doctorId
        if (notes.isNotBlank()) body["notes"] = notes
        api.createPrescription(it, body)
    }
    suspend fun myPrescriptions() = call { api.myPrescriptions(it) }
    suspend fun adminInvoices() = call { api.adminInvoices(it) }
    suspend fun myInvoices() = call { api.myInvoices(it) }
    suspend fun invoice(id: Int) = call { api.invoice(it, id) }
    suspend fun payInvoice(id: Int, method: String, aidName: String, aidNumber: String, medication: Boolean, delivery: Boolean, address: String) = call {
        api.payInvoice(it, id, mapOf(
            "payment_method" to method,
            "medical_aid_name" to aidName,
            "medical_aid_number" to aidNumber,
            "medication_included" to medication,
            "delivery_required" to delivery,
            "delivery_address" to address
        ))
    }
    suspend fun notifications() = call { api.notifications(it) }
    suspend fun markNotificationRead(id: Int) = call { api.markNotificationRead(it, id) }
}
