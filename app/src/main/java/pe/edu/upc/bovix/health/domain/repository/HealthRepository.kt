package pe.edu.upc.bovix.health.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.health.domain.model.HealthSummary

interface HealthRepository {
    fun getHealthSummary(): Flow<Resource<HealthSummary>>
}
