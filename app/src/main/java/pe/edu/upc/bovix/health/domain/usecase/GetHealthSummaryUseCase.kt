package pe.edu.upc.bovix.health.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.health.domain.model.HealthSummary
import pe.edu.upc.bovix.health.domain.repository.HealthRepository
import javax.inject.Inject

class GetHealthSummaryUseCase @Inject constructor(
    private val repository: HealthRepository
) {
    operator fun invoke(): Flow<Resource<HealthSummary>> = repository.getHealthSummary()
}
