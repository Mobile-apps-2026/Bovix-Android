package pe.edu.upc.bovix.health.data.remote.dto

import com.google.gson.annotations.SerializedName

// ─── Appointments ─────────────────────────────────────────────────────────────

data class AppointmentResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("veterinarianName") val veterinarianName: String,
    @SerializedName("scheduledAt") val scheduledAt: String,
    @SerializedName("lot") val lot: String?,
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("userId") val userId: Int = 0,
    @SerializedName("vetId") val vetId: Int = 0
)

data class CreateAppointmentDto(
    @SerializedName("veterinarianName") val veterinarianName: String,
    @SerializedName("scheduledAt") val scheduledAt: String,
    @SerializedName("lot") val lot: String?,
    @SerializedName("status") val status: String = "SCHEDULED",
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("vetId") val vetId: Int = 0
)

// ─── Vets ─────────────────────────────────────────────────────────────────────

data class VetDto(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String
)

data class UpdateAppointmentDto(
    @SerializedName("veterinarianName") val veterinarianName: String,
    @SerializedName("scheduledAt") val scheduledAt: String,
    @SerializedName("lot") val lot: String?,
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String? = null
)

// ─── Clinical Records ─────────────────────────────────────────────────────────

data class ClinicalRecordResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("bovineId") val bovineId: Int,
    @SerializedName("recordDate") val recordDate: String,
    @SerializedName("diagnosis") val diagnosis: String,
    @SerializedName("treatment") val treatment: String?,
    @SerializedName("severity") val severity: String,
    @SerializedName("veterinarianName") val veterinarianName: String?
)

data class CreateClinicalRecordDto(
    @SerializedName("bovineId") val bovineId: Int,
    @SerializedName("recordDate") val recordDate: String,
    @SerializedName("diagnosis") val diagnosis: String,
    @SerializedName("treatment") val treatment: String? = null,
    @SerializedName("severity") val severity: String = "LOW",
    @SerializedName("veterinarianName") val veterinarianName: String? = null
)

// ─── Vaccines ─────────────────────────────────────────────────────────────────

data class VaccineResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("vaccineType") val vaccineType: String?,
    @SerializedName("vaccineDate") val vaccineDate: String?,
    @SerializedName("bovineId") val bovineId: Int
)

// ─── Bovines (for health cross-reference) ─────────────────────────────────────

data class BovineForHealthDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("lot") val lot: String?
)
