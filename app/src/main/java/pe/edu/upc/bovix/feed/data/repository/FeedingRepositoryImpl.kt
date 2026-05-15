package pe.edu.upc.bovix.feed.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.feed.data.local.FeedingDao
import pe.edu.upc.bovix.feed.data.mapper.toDomain
import pe.edu.upc.bovix.feed.data.mapper.toEntity
import pe.edu.upc.bovix.feed.data.remote.FeedingApi
import pe.edu.upc.bovix.feed.data.remote.dto.FeedingComponentDto
import pe.edu.upc.bovix.feed.data.remote.dto.FeedingPlanDto
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

        // 1) Cache local (offline-first)
        val cached = loadFromCache()
        if (cached.isNotEmpty()) emit(Resource.Success(cached))

        // 2) Refrescar desde la red (con fallback demo)
        try {
            val remote = fetchRemoteOrFallback()

            dao.clearComponents()
            dao.clearPlans()
            dao.upsertPlans(remote.map { it.toEntity() })
            remote.forEach { plan ->
                dao.upsertComponents(plan.components.map { it.toEntity(plan.id) })
            }

            emit(Resource.Success(remote.map { it.toDomain() }))
        } catch (io: IOException) {
            if (cached.isEmpty()) emit(Resource.Error("Sin conexión", io))
        } catch (e: Exception) {
            if (cached.isEmpty())
                emit(Resource.Error(e.localizedMessage ?: "Error al cargar Alimentación", e))
        }
    }

    private suspend fun loadFromCache(): List<FeedingPlan> {
        val plans = dao.getPlans()
        return plans.map { plan ->
            val comps = dao.getComponentsForPlan(plan.id)
            plan.toDomain(comps)
        }
    }

    /* dejar api.getFeedingPlans() cuando se necesite conectar al api */
    private suspend fun fetchRemoteOrFallback(): List<FeedingPlanDto> = try {
        api.getFeedingPlans()
    } catch (_: Throwable) {
        listOf(
            FeedingPlanDto(
                id = "plan-A",
                lot = "A",
                dailyRationKg = 8.5,
                animalCount = 28,
                components = listOf(
                    FeedingComponentDto("c1", "Heno de alfalfa", 40, 3.4, "MINT"),
                    FeedingComponentDto("c2", "Maíz molido", 30, 2.55, "AMBER"),
                    FeedingComponentDto("c3", "Pasto verde", 20, 1.7, "GREEN"),
                    FeedingComponentDto("c4", "Concentrado", 10, 0.85, "SKY")
                )
            ),
            FeedingPlanDto(
                id = "plan-B",
                lot = "B",
                dailyRationKg = 7.0,
                animalCount = 22,
                components = listOf(
                    FeedingComponentDto("c5", "Heno de alfalfa", 50, 3.5, "MINT"),
                    FeedingComponentDto("c6", "Maíz molido", 25, 1.75, "AMBER"),
                    FeedingComponentDto("c7", "Pasto verde", 15, 1.05, "GREEN"),
                    FeedingComponentDto("c8", "Concentrado", 10, 0.7, "SKY")
                )
            ),
            FeedingPlanDto(
                id = "plan-C",
                lot = "C",
                dailyRationKg = 6.0,
                animalCount = 18,
                components = listOf(
                    FeedingComponentDto("c9", "Heno de alfalfa", 35, 2.1, "MINT"),
                    FeedingComponentDto("c10", "Maíz molido", 35, 2.1, "AMBER"),
                    FeedingComponentDto("c11", "Pasto verde", 20, 1.2, "GREEN"),
                    FeedingComponentDto("c12", "Concentrado", 10, 0.6, "SKY")
                )
            )
        )
    }
}
