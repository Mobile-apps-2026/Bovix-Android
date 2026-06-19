package pe.edu.upc.bovix.health.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.health.domain.model.AlertSeverity
import pe.edu.upc.bovix.health.domain.model.HealthSummary
import java.time.LocalDateTime

interface HealthRepository {
    fun getHealthSummary(): Flow<Resource<HealthSummary>>
    suspend fun scheduleAppointment(veterinarianName: String, lot: String?, scheduledAt: LocalDateTime)
    suspend fun cancelAppointment(id: String)
    suspend fun addClinicalEntry(bovineId: Int, diagnosis: String, treatment: String?, severity: AlertSeverity?, veterinarianName: String?)
    suspend fun markVaccineDone(id: String)
}
