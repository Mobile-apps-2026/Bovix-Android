package pe.edu.upc.bovix.home.data.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.cattle.data.local.AnimalDao
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.core.common.resourceFlow
import pe.edu.upc.bovix.feed.data.local.FeedingDao
import pe.edu.upc.bovix.home.data.local.HomeStatsDao
import pe.edu.upc.bovix.home.data.mapper.toDomain
import pe.edu.upc.bovix.home.data.mapper.toEntity
import pe.edu.upc.bovix.home.data.remote.HomeApi
import pe.edu.upc.bovix.home.data.remote.dto.HomeActivityDto
import pe.edu.upc.bovix.home.data.remote.dto.HomeResponseDto
import pe.edu.upc.bovix.home.data.remote.dto.HomeStatsDto
import pe.edu.upc.bovix.home.domain.model.HomeData
import pe.edu.upc.bovix.home.domain.repository.HomeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val api: HomeApi,
    private val dao: HomeStatsDao,
    private val animalDao: AnimalDao,
    private val feedingDao: FeedingDao
) : HomeRepository {

    override fun getHomeData(): Flow<Resource<HomeData>> = resourceFlow(
        loadCache = { dao.get()?.toDomain() },
        fetch = {
            val dto = fetchRemoteOrFallback()
            dao.upsert(dto.toEntity())
            dto.toDomain()
        },
        errorMessage = "Error al cargar inicio"
    )

    // Sin backend activo cae al datos locales; cambiar por api.getSummary() cuando esté disponible.
    private suspend fun fetchRemoteOrFallback(): HomeResponseDto {
        return try {
            api.getSummary()
        } catch (_: Throwable) {
            val animals = animalDao.getAll()
            val totalAnimals = animals.size
            val activeLots = animals.map { it.lot }.filter { it.isNotBlank() }.distinct().size

            val activities = buildRecentActivities()

            HomeResponseDto(
                userName = "Juan Quispe",
                stats = HomeStatsDto(
                    totalAnimals = totalAnimals,
                    activeLots = activeLots,
                    appointmentsToday = 0,
                    alerts = 0
                ),
                alert = null,
                activities = activities
            )
        }
    }

    private suspend fun buildRecentActivities(): List<HomeActivityDto> {
        val items = mutableListOf<Pair<Long, HomeActivityDto>>()

        animalDao.getAll()
            .sortedByDescending { it.createdAt }
            .take(4)
            .forEach { animal ->
                items.add(animal.createdAt to HomeActivityDto(
                    id = "animal_${animal.id}",
                    title = "${animal.name} registrado en Lote ${animal.lot}",
                    subtitle = relativeTime(animal.createdAt),
                    type = "REGISTRATION"
                ))
            }

        feedingDao.getPlans()
            .sortedByDescending { it.createdAt }
            .take(3)
            .forEach { plan ->
                items.add(plan.createdAt to HomeActivityDto(
                    id = "plan_${plan.id}",
                    title = "Plan alimentario – Lote ${plan.lot}",
                    subtitle = relativeTime(plan.createdAt),
                    type = "FEED_UPDATE"
                ))
            }

        return items
            .sortedByDescending { it.first }
            .take(5)
            .map { it.second }
    }

    private fun relativeTime(timestamp: Long): String {
        val diffMin = (System.currentTimeMillis() - timestamp) / 60_000
        return when {
            diffMin < 1    -> "Ahora mismo"
            diffMin < 60   -> "Hace ${diffMin}min"
            diffMin < 1440 -> "Hace ${diffMin / 60}h"
            else           -> "Hace ${diffMin / 1440}d"
        }
    }
}
