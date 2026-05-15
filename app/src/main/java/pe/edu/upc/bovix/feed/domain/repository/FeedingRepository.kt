package pe.edu.upc.bovix.feed.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.feed.domain.model.FeedingPlan

interface FeedingRepository {
    /** Lista de planes (uno por lote disponible). */
    fun getFeedingPlans(): Flow<Resource<List<FeedingPlan>>>
}
