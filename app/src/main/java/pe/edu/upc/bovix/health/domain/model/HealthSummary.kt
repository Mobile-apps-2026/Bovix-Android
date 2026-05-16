package pe.edu.upc.bovix.health.domain.model

import java.time.LocalDateTime

// Todo lo que necesita la pantalla de Salud en un solo objeto.
data class HealthSummary(
    val nextAppointment: VetAppointment?,
    val pendingVaccinations: List<PendingVaccination>,
    val clinicalHistory: List<ClinicalEntry>
)

data class VetAppointment(
    val id: String,
    val veterinarianName: String,
    val scheduledAt: LocalDateTime,
    val lots: String,
    val status: AppointmentStatus
)

enum class AppointmentStatus { SCHEDULED, COMPLETED, CANCELLED }

data class PendingVaccination(
    val id: String,
    val vaccineName: String,
    val lotLabel: String,
    val dueLabel: String,
    val severity: AlertSeverity
)

enum class AlertSeverity { HIGH, MEDIUM, LOW }

data class ClinicalEntry(
    val id: String,
    val title: String,
    val dateLabel: String,
    val severity: AlertSeverity?  // null cuando la entrada no tiene severidad (ej. un tratamiento)
)
