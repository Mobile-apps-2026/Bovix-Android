package pe.edu.upc.bovix.health.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vet_appointments")
data class VetAppointmentEntity(
    @PrimaryKey val id: String,
    val veterinarianName: String,
    val scheduledAt: String,    // ISO-8601
    val lots: String,
    val status: String
)

@Entity(tableName = "pending_vaccinations")
data class PendingVaccinationEntity(
    @PrimaryKey val id: String,
    val vaccineName: String,
    val lotLabel: String,
    val dueLabel: String,
    val severity: String
)

@Entity(tableName = "clinical_entries")
data class ClinicalEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val dateLabel: String,
    val severity: String?
)
