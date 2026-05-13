package pe.edu.upc.bovix.home.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.home.data.local.HomeStatsDao
import pe.edu.upc.bovix.home.data.mapper.toDomain
import pe.edu.upc.bovix.home.data.mapper.toEntity
import pe.edu.upc.bovix.home.data.remote.HomeApi
import pe.edu.upc.bovix.home.data.remote.dto.HomeActivityDto
import pe.edu.upc.bovix.home.data.remote.dto.HomeAlertDto
import pe.edu.upc.bovix.home.data.remote.dto.HomeResponseDto
import pe.edu.upc.bovix.home.data.remote.dto.HomeStatsDto
import pe.edu.upc.bovix.home.domain.model.HomeData
import pe.edu.upc.bovix.home.domain.repository.HomeRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val api: HomeApi,
    private val dao: HomeStatsDao
) : HomeRepository {

    override fun getHomeData(): Flow<Resource<HomeData>> = flow {
        emit(Resource.Loading)

        // 1. Emitimos cache local si existe (offline-first)
        dao.get()?.let { emit(Resource.Success(it.toDomain())) }

        // 2. Intentamos refrescar desde la red
        try {
            val remote = fetchRemoteOrFallback()
            dao.upsert(remote.toEntity())
            emit(Resource.Success(remote.toDomain()))
        } catch (io: IOException) {
            // Si ya emitimos cache, no sobreescribimos con error; solo emitimos error
            // si no había cache previo
            if (dao.get() == null) {
                emit(Resource.Error("Sin conexión", io))
            }
        } catch (e: Exception) {
            if (dao.get() == null) {
                emit(Resource.Error(e.localizedMessage ?: "Error al cargar inicio", e))
            }
        }
    }

    /**
     * MODO DEMO: mientras no exista backend, devolvemos datos del mockup.
     * Cuando el endpoint esté listo, reemplazar por `api.getSummary()`.
     */
    private suspend fun fetchRemoteOrFallback(): HomeResponseDto {
        return try {
            api.getSummary()
        } catch (_: Throwable) {
            HomeResponseDto(
                userName = "Juan Quispe",
                stats = HomeStatsDto(
                    totalAnimals = 84,
                    activeLots = 3,
                    appointmentsToday = 2,
                    alerts = 1
                ),
                alert = HomeAlertDto(
                    title = "Vacuna pendiente",
                    description = "Lote B (22 animales) – vence mañana"
                ),
                activities = listOf(
                    HomeActivityDto("a1", "Animal #047 registrado", "Lote A · Hace 2h", "REGISTRATION"),
                    HomeActivityDto("a2", "Plan alimentario actualizado", "Lote C · Ayer", "FEED_UPDATE"),
                    HomeActivityDto("a3", "Visita veterinaria completada", "Dr. Bottger · Ayer", "VET_VISIT")
                )
            )
        }
    }
}
