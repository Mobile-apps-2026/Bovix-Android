package pe.edu.upc.bovix.health.data.mapper

import pe.edu.upc.bovix.health.data.local.ClinicalEntryEntity
import pe.edu.upc.bovix.health.data.local.PendingVaccinationEntity
import pe.edu.upc.bovix.health.data.local.VetAppointmentEntity
import pe.edu.upc.bovix.health.data.remote.dto.AppointmentResponseDto
import pe.edu.upc.bovix.health.data.remote.dto.BovineForHealthDto
import pe.edu.upc.bovix.health.data.remote.dto.ClinicalRecordResponseDto
import pe.edu.upc.bovix.health.data.remote.dto.VaccineResponseDto
import pe.edu.upc.bovix.health.domain.model.AlertSeverity
import pe.edu.upc.bovix.health.domain.model.AppointmentStatus
import pe.edu.upc.bovix.health.domain.model.ClinicalEntry
import pe.edu.upc.bovix.health.domain.model.PendingVaccination
import pe.edu.upc.bovix.health.domain.model.VetAppointment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─── DTO -> Domain ────────────────────────────────────────────────────────────

fun AppointmentResponseDto.toDomain(): VetAppointment = VetAppointment(
    id = id.toString(),
    veterinarianName = veterinarianName,
    scheduledAt = parseDate(scheduledAt),
    lots = lot ?: "",
    status = parseStatus(status)
)

fun VaccineResponseDto.toPendingVaccination(bovineName: String, bovineLot: String?): PendingVaccination {
    val dateLabel = vaccineDate?.let { formatVaccineDate(it) } ?: "Fecha no definida"
    val lotLabel = if (bovineLot != null) "Lote $bovineLot – $bovineName" else bovineName
    return PendingVaccination(
        id = id.toString(),
        vaccineName = name,
        lotLabel = lotLabel,
        dueLabel = dateLabel,
        severity = AlertSeverity.LOW
    )
}

fun ClinicalRecordResponseDto.toClinicalEntry(bovineName: String?): ClinicalEntry {
    val dateLabel = recordDate.substring(0, 10)
    val title = buildString {
        append(diagnosis)
        treatment?.let { append(" · $it") }
        if (!veterinarianName.isNullOrBlank()) append(" · ${veterinarianName}")
    }
    return ClinicalEntry(
        id = id.toString(),
        title = title,
        dateLabel = dateLabel,
        severity = parseSeverity(severity)
    )
}

// ─── DTO -> Entity (para caché local) ────────────────────────────────────────

fun AppointmentResponseDto.toEntity(): VetAppointmentEntity = VetAppointmentEntity(
    id = id.toString(), veterinarianName = veterinarianName,
    scheduledAt = scheduledAt, lots = lot ?: "", status = status
)

fun PendingVaccination.toEntity(): PendingVaccinationEntity = PendingVaccinationEntity(
    id = id, vaccineName = vaccineName,
    lotLabel = lotLabel, dueLabel = dueLabel, severity = severity.name
)

fun ClinicalEntry.toEntity(): ClinicalEntryEntity = ClinicalEntryEntity(
    id = id, title = title, dateLabel = dateLabel, severity = severity?.name
)

// ─── Entity -> Domain ─────────────────────────────────────────────────────────

fun VetAppointmentEntity.toDomain(): VetAppointment = VetAppointment(
    id = id,
    veterinarianName = veterinarianName,
    scheduledAt = parseDate(scheduledAt),
    lots = lots,
    status = parseStatus(status)
)

fun PendingVaccinationEntity.toDomain(): PendingVaccination = PendingVaccination(
    id = id,
    vaccineName = vaccineName,
    lotLabel = lotLabel,
    dueLabel = dueLabel,
    severity = parseSeverity(severity) ?: AlertSeverity.LOW
)

fun ClinicalEntryEntity.toDomain(): ClinicalEntry = ClinicalEntry(
    id = id,
    title = title,
    dateLabel = dateLabel,
    severity = parseSeverity(severity)
)

// ─── Helpers ──────────────────────────────────────────────────────────────────

fun parseStatus(raw: String): AppointmentStatus =
    runCatching { AppointmentStatus.valueOf(raw.uppercase()) }
        .getOrDefault(AppointmentStatus.SCHEDULED)

fun parseSeverity(raw: String?): AlertSeverity? {
    if (raw.isNullOrBlank()) return null
    return runCatching { AlertSeverity.valueOf(raw.uppercase()) }.getOrNull()
}

fun parseDate(raw: String): LocalDateTime = runCatching {
    LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}.getOrElse {
    runCatching { LocalDateTime.parse(raw) }.getOrDefault(LocalDateTime.now())
}

private fun formatVaccineDate(raw: String): String = runCatching {
    val date = LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
    date.format(DateTimeFormatter.ofPattern("d MMM", Locale("es")))
}.getOrElse { raw.substring(0, 10) }
