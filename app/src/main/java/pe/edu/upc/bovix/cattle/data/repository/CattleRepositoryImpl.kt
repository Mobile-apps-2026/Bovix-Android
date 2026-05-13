package pe.edu.upc.bovix.cattle.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upc.bovix.cattle.data.local.AnimalDao
import pe.edu.upc.bovix.cattle.data.mapper.toDomain
import pe.edu.upc.bovix.cattle.data.mapper.toEntity
import pe.edu.upc.bovix.cattle.data.remote.CattleApi
import pe.edu.upc.bovix.cattle.data.remote.dto.AnimalDto
import pe.edu.upc.bovix.cattle.domain.model.Animal
import pe.edu.upc.bovix.cattle.domain.repository.CattleRepository
import pe.edu.upc.bovix.core.common.Resource
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CattleRepositoryImpl @Inject constructor(
    private val api: CattleApi,
    private val dao: AnimalDao
) : CattleRepository {

    override fun getAnimals(): Flow<Resource<List<Animal>>> = flow {
        emit(Resource.Loading)

        // 1) Cache local primero (offline-first)
        val cached = dao.getAll()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached.map { it.toDomain() }))
        }

        // 2) Refrescar desde red (con fallback a datos demo)
        try {
            val remote = fetchRemoteOrFallback()
            dao.upsertAll(remote.map { it.toEntity() })
            emit(Resource.Success(remote.map { it.toDomain() }))
        } catch (io: IOException) {
            if (cached.isEmpty()) emit(Resource.Error("Sin conexión", io))
        } catch (e: Exception) {
            if (cached.isEmpty()) emit(Resource.Error(e.localizedMessage ?: "Error al cargar ganado", e))
        }
    }

    /**
     * MODO DEMO: mientras no haya backend, devuelve datos del mockup.
     * Reemplazar por `api.getAnimals()` cuando el endpoint esté listo.
     */
    private suspend fun fetchRemoteOrFallback(): List<AnimalDto> = try {
        api.getAnimals()
    } catch (_: Throwable) {
        listOf(
            AnimalDto("#047", "Toro Simmental", "A", "HEALTHY", "MALE", 420),
            AnimalDto("#031", "Vaca Holstein", "A", "MONITORED", "FEMALE", 380),
            AnimalDto("#062", "Ternero Angus", "B", "HEALTHY", "MALE", 120),
            AnimalDto("#018", "Vaca Brahman", "B", "QUARANTINE", "FEMALE", 350),
            AnimalDto("#073", "Toro Charolais", "C", "HEALTHY", "MALE", 510),
            AnimalDto("#024", "Vaca Jersey", "C", "MONITORED", "FEMALE", 340)
        )
    }
}
