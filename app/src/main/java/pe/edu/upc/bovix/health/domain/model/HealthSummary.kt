package pe.edu.upc.bovix.health.domain.model

import java.time.LocalDateTime

/**
 * Agregado para la pantalla de Salud.
 * Combina próxima cita + vacunación pendiente + historial clínico.
 */
data class HealthSummary(
    val nextAppointment: VetAppointment?,
    val pendingVaccinations: List<PendingVaccination>,
    val clinicalHistory: List<ClinicalEntry>
)

data class VetAppointment(
    val id: String,
    val veterinarianName: String,
    val scheduledAt: LocalDateTime,
    val lots: String,                // ej. "Lote A y B"
    val status: AppointmentStatus
)

enum class AppointmentStatus { SCHEDULED, COMPLETED, CANCELLED }

data class PendingVaccination(
    val id: String,
    val vaccineName: String,         // ej. "Fiebre aftosa"
    val lotLabel: String,            // ej. "Lote B (22 animales)"
    val dueLabel: String,            // ej. "Mañana", "15 jun"
    val severity: AlertSeverity
)

enum class AlertSeverity { HIGH, MEDIUM, LOW }

data class ClinicalEntry(
    val id: String,
    val title: String,               // ej. "Diagnóstico: Animal #018 – Mastitis leve"
    val dateLabel: String,           // ej. "8 may"
    val severity: AlertSeverity?     // null si no aplica (ej. tratamientos)
)
