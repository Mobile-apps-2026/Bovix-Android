package pe.edu.upc.bovix.health.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AppointmentDto(
    @SerializedName("id") val id: String,
    @SerializedName("veterinarianName") val veterinarianName: String,
    @SerializedName("scheduledAt") val scheduledAt: String,   // ISO-8601
    @SerializedName("lots") val lots: String,
    @SerializedName("status") val status: String              // SCHEDULED/COMPLETED/CANCELLED
)

data class PendingVaccineDto(
    @SerializedName("id") val id: String,
    @SerializedName("vaccineName") val vaccineName: String,
    @SerializedName("lotLabel") val lotLabel: String,
    @SerializedName("dueLabel") val dueLabel: String,
    @SerializedName("severity") val severity: String          // HIGH/MEDIUM/LOW
)

data class ClinicalRecordDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("dateLabel") val dateLabel: String,
    @SerializedName("severity") val severity: String?
)
