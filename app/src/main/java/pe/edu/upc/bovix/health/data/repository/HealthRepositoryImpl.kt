package pe.edu.upc.bovix.health.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.health.data.local.HealthDao
import pe.edu.upc.bovix.health.data.mapper.toDomain
import pe.edu.upc.bovix.health.data.mapper.toEntity
import pe.edu.upc.bovix.health.data.remote.HealthApi
import pe.edu.upc.bovix.health.data.remote.dto.AppointmentDto
import pe.edu.upc.bovix.health.data.remote.dto.ClinicalRecordDto
import pe.edu.upc.bovix.health.data.remote.dto.PendingVaccineDto
import pe.edu.upc.bovix.health.domain.model.HealthSummary
import pe.edu.upc.bovix.health.domain.repository.HealthRepository
import java.io.IOException
import java.time.LocalDateTime
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
            val appointment = fetchAppointmentOrFallback()
            val pending = fetchPendingOrFallback()
            val history = fetchHistoryOrFallback()

            dao.clearAppointments()
            appointment?.let { dao.upsertAppointment(it.toEntity()) }
            dao.clearPendingVaccinations()
            dao.upsertPendingVaccinations(pending.map { it.toEntity() })
            dao.clearClinicalEntries()
            dao.upsertClinicalEntries(history.map { it.toEntity() })

            emit(
                Resource.Success(
                    HealthSummary(
                        nextAppointment = appointment?.toDomain(),
                        pendingVaccinations = pending.map { it.toDomain() },
                        clinicalHistory = history.map { it.toDomain() }
                    )
                )
            )
        } catch (io: IOException) {
            if (!cachedSummary.hasContent()) emit(Resource.Error("Sin conexión", io))
        } catch (e: Exception) {
            if (!cachedSummary.hasContent())
                emit(Resource.Error(e.localizedMessage ?: "Error al cargar Salud", e))
        }
    }

    private suspend fun buildCachedSummary(): HealthSummary = HealthSummary(
        nextAppointment = dao.getNextAppointment()?.toDomain(),
        pendingVaccinations = dao.getPendingVaccinations().map { it.toDomain() },
        clinicalHistory = dao.getClinicalEntries().map { it.toDomain() }
    )

    private fun HealthSummary.hasContent(): Boolean =
        nextAppointment != null || pendingVaccinations.isNotEmpty() || clinicalHistory.isNotEmpty()

    // Cada función intenta el endpoint real; si falla vuelve a datos de demo.
    private suspend fun fetchAppointmentOrFallback(): AppointmentDto? = try {
        api.getNextAppointment()
    } catch (_: Throwable) {
        AppointmentDto(
            id = "ap-1",
            veterinarianName = "Dr. Johan Bottger",
            scheduledAt = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).toString(),
            lots = "Lote A y B",
            status = "SCHEDULED"
        )
    }

    private suspend fun fetchPendingOrFallback(): List<PendingVaccineDto> = try {
        api.getPendingVaccines()
    } catch (_: Throwable) {
        listOf(
            PendingVaccineDto("pv-1", "Fiebre aftosa", "Lote B (22 animales)", "Mañana", "HIGH"),
            PendingVaccineDto("pv-2", "Brucelosis", "Lote C (18 animales)", "15 jun", "MEDIUM")
        )
    }

    private suspend fun fetchHistoryOrFallback(): List<ClinicalRecordDto> = try {
        api.getClinicalRecords()
    } catch (_: Throwable) {
        listOf(
            ClinicalRecordDto(
                "cr-1",
                "Diagnóstico: Animal #018 – Mastitis leve",
                "8 may",
                "MEDIUM"
            ),
            ClinicalRecordDto(
                "cr-2",
                "Tratamiento: Antibiótico 7 días · Dr. Bottger",
                "8 may",
                null
            )
        )
    }
}
