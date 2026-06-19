package pe.edu.upc.bovix.feed.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.feed.data.local.FeedingDao
import pe.edu.upc.bovix.feed.data.mapper.toDomain
import pe.edu.upc.bovix.feed.data.mapper.toEntity
import pe.edu.upc.bovix.feed.data.remote.FeedingApi
import pe.edu.upc.bovix.feed.data.remote.dto.AddFeedingComponentDto
import pe.edu.upc.bovix.feed.data.remote.dto.CreateFeedingPlanDto
import pe.edu.upc.bovix.feed.data.remote.dto.UpdateFeedingComponentDto
import pe.edu.upc.bovix.feed.domain.model.FeedingComponent
import pe.edu.upc.bovix.feed.domain.model.FeedingPlan
import pe.edu.upc.bovix.feed.domain.repository.FeedingRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedingRepositoryImpl @Inject constructor(
    private val api: FeedingApi,
    private val dao: FeedingDao
) : FeedingRepository {

    override fun getFeedingPlans(): Flow<Resource<List<FeedingPlan>>> = flow {
        emit(Resource.Loading)

        val cached = loadFromCache()
        if (cached.isNotEmpty()) emit(Resource.Success(cached))

        try {
            val remote = api.getFeedingPlans()
            dao.clearComponents()
            dao.clearPlans()
            dao.upsertPlans(remote.map { it.toEntity() })
            remote.forEach { plan ->
                dao.upsertComponents(plan.components.mapIndexed { i, c -> c.toEntity(plan.id.toString(), i) })
            }
            emit(Resource.Success(remote.map { it.toDomain() }))
        } catch (io: IOException) {
            if (cached.isEmpty()) emit(Resource.Error("Sin conexión", io))
        } catch (e: Exception) {
            if (cached.isEmpty())
                emit(Resource.Error(e.localizedMessage ?: "Error al cargar Alimentación", e))
        }
    }

    override suspend fun createPlan(
        lot: String, dailyRationKg: Double, animalCount: Int, components: List<FeedingComponent>
    ): FeedingPlan {
        val planDto = api.createFeedingPlan(CreateFeedingPlanDto(lot, dailyRationKg, animalCount))
        components.forEach { c ->
            api.addComponent(planDto.id, AddFeedingComponentDto(c.name, c.percentage, c.amountKg))
        }
        val updated = api.getFeedingPlans().find { it.id == planDto.id } ?: planDto
        return updated.toDomain()
    }

    // Con los nuevos endpoints de componentes podemos editar el plan in-place:
    // 1. PUT plan metadata
    // 2. DELETE componentes removidos
    // 3. PUT componentes modificados
    // 4. POST componentes nuevos
    override suspend fun savePlanEdit(
        planId: String, lot: String, dailyRationKg: Double, animalCount: Int, components: List<FeedingComponent>
    ): FeedingPlan {
        val intPlanId = planId.toIntOrNull()
            ?: return createPlan(lot, dailyRationKg, animalCount, components)

        // Fetch the current plan to know which components already exist server-side
        val currentPlans = api.getFeedingPlans()
        val currentPlan = currentPlans.find { it.id == intPlanId }
        val existingComponents = currentPlan?.components ?: emptyList()
        val existingIds = existingComponents.map { it.id }.toSet()
        val incomingIds = components.mapNotNull { it.id.toIntOrNull() }.toSet()

        // Delete components that were removed
        for (existing in existingComponents) {
            if (existing.id !in incomingIds) {
                api.deleteComponent(intPlanId, existing.id)
            }
        }

        // Update or create components
        for (component in components) {
            val componentIntId = component.id.toIntOrNull()
            if (componentIntId != null && componentIntId in existingIds) {
                api.updateComponent(
                    intPlanId, componentIntId,
                    UpdateFeedingComponentDto(component.name, component.percentage, component.amountKg)
                )
            } else {
                api.addComponent(intPlanId, AddFeedingComponentDto(component.name, component.percentage, component.amountKg))
            }
        }

        val updatedPlans = api.getFeedingPlans()
        return updatedPlans.find { it.id == intPlanId }?.toDomain()
            ?: createPlan(lot, dailyRationKg, animalCount, components)
    }

    override suspend fun deletePlan(planId: String) {
        val id = planId.toIntOrNull() ?: return
        api.deleteFeedingPlan(id)
    }

    private suspend fun loadFromCache(): List<FeedingPlan> {
        val plans = dao.getPlans()
        return plans.map { plan ->
            val comps = dao.getComponentsForPlan(plan.id)
            plan.toDomain(comps)
        }
    }
}
