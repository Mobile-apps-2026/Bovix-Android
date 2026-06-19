package pe.edu.upc.bovix.feed.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.feed.domain.model.FeedingComponent
import pe.edu.upc.bovix.feed.domain.model.FeedingPlan

interface FeedingRepository {
    fun getFeedingPlans(): Flow<Resource<List<FeedingPlan>>>
    suspend fun createPlan(lot: String, dailyRationKg: Double, animalCount: Int, components: List<FeedingComponent>): FeedingPlan
    suspend fun savePlanEdit(planId: String, lot: String, dailyRationKg: Double, animalCount: Int, components: List<FeedingComponent>): FeedingPlan
    suspend fun deletePlan(planId: String)
}
