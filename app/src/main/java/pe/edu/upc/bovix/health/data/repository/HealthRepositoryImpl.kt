package pe.edu.upc.bovix.health.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.health.data.local.HealthDao
import pe.edu.upc.bovix.health.data.mapper.parseDate
import pe.edu.upc.bovix.health.data.mapper.parseSeverity
import pe.edu.upc.bovix.health.data.mapper.toClinicalEntry
import pe.edu.upc.bovix.health.data.mapper.toDomain
import pe.edu.upc.bovix.health.data.mapper.toEntity
import pe.edu.upc.bovix.health.data.mapper.toPendingVaccination
import pe.edu.upc.bovix.health.data.remote.HealthApi
import pe.edu.upc.bovix.health.data.remote.dto.CreateAppointmentDto
import pe.edu.upc.bovix.health.data.remote.dto.CreateClinicalRecordDto
import pe.edu.upc.bovix.health.domain.model.AlertSeverity
import pe.edu.upc.bovix.health.domain.model.AppointmentStatus
import pe.edu.upc.bovix.health.domain.model.HealthSummary
import pe.edu.upc.bovix.health.domain.repository.HealthRepository
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val api: HealthApi,
    private val dao: HealthDao
) : HealthRepository {

    override fun getHealthSummary(): Flow<Resource<HealthSummary>> = flow {
        emit(Resource.Loading)

        val cachedSummary = buildCachedSummary()
        if (cachedSummary.hasContent()) emit(Resource.Success(cachedSummary))

        try {
            val bovines = api.getBovines()
            val bovineMap = bovines.associate { it.id to (it.name to it.lot) }

            val appointments = api.getAppointments()
            val nextDto = appointments
                .filter { it.status.equals("SCHEDULED", ignoreCase = true) }
                .minByOrNull { parseDate(it.scheduledAt) }

            val vaccines = api.getVaccines()
            val pendingVaccinations = vaccines.map { v ->
                val (name, lot) = bovineMap[v.bovineId] ?: ("Bovino #${v.bovineId}" to null)
                v.toPendingVaccination(name, lot)
            }

            val records = api.getClinicalRecords()
            val clinicalHistory = records.map { r ->
                val bovineName = bovineMap[r.bovineId]?.first
                r.toClinicalEntry(bovineName)
            }.sortedByDescending { it.dateLabel }

            val availableBovines = bovines.map { it.id to it.name }

            // Cache to local DB using DTO-level mappers
            dao.clearAppointments()
            nextDto?.let { dao.upsertAppointment(it.toEntity()) }
            dao.clearPendingVaccinations()
            dao.upsertPendingVaccinations(pendingVaccinations.map { it.toEntity() })
            dao.clearClinicalEntries()
            dao.upsertClinicalEntries(clinicalHistory.map { it.toEntity() })

            emit(Resource.Success(HealthSummary(
                nextAppointment = nextDto?.toDomain(),
                pendingVaccinations = pendingVaccinations,
                clinicalHistory = clinicalHistory,
                availableBovines = availableBovines
            )))
        } catch (io: IOException) {
            if (!cachedSummary.hasContent()) emit(Resource.Error("Sin conexión", io))
        } catch (e: Exception) {
            if (!cachedSummary.hasContent())
                emit(Resource.Error(e.localizedMessage ?: "Error al cargar Salud", e))
        }
    }

    override suspend fun scheduleAppointment(
        veterinarianName: String, lot: String?, scheduledAt: LocalDateTime
    ) {
        api.createAppointment(
            CreateAppointmentDto(
                veterinarianName = veterinarianName,
                scheduledAt = scheduledAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                lot = lot,
                status = AppointmentStatus.SCHEDULED.name
            )
        )
    }

    override suspend fun cancelAppointment(id: String) {
        val intId = id.toIntOrNull() ?: return
        api.deleteAppointment(intId)
    }

    override suspend fun addClinicalEntry(
        bovineId: Int, diagnosis: String, treatment: String?,
        severity: AlertSeverity?, veterinarianName: String?
    ) {
        api.createClinicalRecord(
            CreateClinicalRecordDto(
                bovineId = bovineId,
                recordDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                diagnosis = diagnosis,
                treatment = treatment,
                severity = severity?.name ?: AlertSeverity.LOW.name,
                veterinarianName = veterinarianName
            )
        )
    }

    override suspend fun markVaccineDone(id: String) {
        val intId = id.toIntOrNull() ?: return
        api.deleteVaccine(intId)
    }

    private suspend fun buildCachedSummary(): HealthSummary = HealthSummary(
        nextAppointment = dao.getNextAppointment()?.toDomain(),
        pendingVaccinations = dao.getPendingVaccinations().map { it.toDomain() },
        clinicalHistory = dao.getClinicalEntries().map { it.toDomain() }
    )

    private fun HealthSummary.hasContent(): Boolean =
        nextAppointment != null || pendingVaccinations.isNotEmpty() || clinicalHistory.isNotEmpty()
}
