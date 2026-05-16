package pe.edu.upc.bovix.health.data.mapper

import pe.edu.upc.bovix.health.data.local.ClinicalEntryEntity
import pe.edu.upc.bovix.health.data.local.PendingVaccinationEntity
import pe.edu.upc.bovix.health.data.local.VetAppointmentEntity
import pe.edu.upc.bovix.health.data.remote.dto.AppointmentDto
import pe.edu.upc.bovix.health.data.remote.dto.ClinicalRecordDto
import pe.edu.upc.bovix.health.data.remote.dto.PendingVaccineDto
import pe.edu.upc.bovix.health.domain.model.AlertSeverity
import pe.edu.upc.bovix.health.domain.model.AppointmentStatus
import pe.edu.upc.bovix.health.domain.model.ClinicalEntry
import pe.edu.upc.bovix.health.domain.model.PendingVaccination
import pe.edu.upc.bovix.health.domain.model.VetAppointment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// DTO -> Domain

fun AppointmentDto.toDomain(): VetAppointment = VetAppointment(
    id = id,
    veterinarianName = veterinarianName,
    scheduledAt = parseDate(scheduledAt),
    lots = lots,
    status = parseStatus(status)
)

fun PendingVaccineDto.toDomain(): PendingVaccination = PendingVaccination(
    id = id,
    vaccineName = vaccineName,
    lotLabel = lotLabel,
    dueLabel = dueLabel,
    severity = parseSeverity(severity) ?: AlertSeverity.LOW
)

fun ClinicalRecordDto.toDomain(): ClinicalEntry = ClinicalEntry(
    id = id,
    title = title,
    dateLabel = dateLabel,
    severity = parseSeverity(severity)
)

// DTO -> Entity

fun AppointmentDto.toEntity(): VetAppointmentEntity = VetAppointmentEntity(
    id = id, veterinarianName = veterinarianName,
    scheduledAt = scheduledAt, lots = lots, status = status
)

fun PendingVaccineDto.toEntity(): PendingVaccinationEntity = PendingVaccinationEntity(
    id = id, vaccineName = vaccineName,
    lotLabel = lotLabel, dueLabel = dueLabel, severity = severity
)

fun ClinicalRecordDto.toEntity(): ClinicalEntryEntity = ClinicalEntryEntity(
    id = id, title = title, dateLabel = dateLabel, severity = severity
)

// Entity -> Domain

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

// Helpers de parseo de strings del backend

private fun parseStatus(raw: String): AppointmentStatus =
    runCatching { AppointmentStatus.valueOf(raw.uppercase()) }
        .getOrDefault(AppointmentStatus.SCHEDULED)

private fun parseSeverity(raw: String?): AlertSeverity? {
    if (raw.isNullOrBlank()) return null
    return runCatching { AlertSeverity.valueOf(raw.uppercase()) }.getOrNull()
}

private fun parseDate(raw: String): LocalDateTime = runCatching {
    LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}.getOrElse {
    runCatching { LocalDateTime.parse(raw) }.getOrDefault(LocalDateTime.now())
}
