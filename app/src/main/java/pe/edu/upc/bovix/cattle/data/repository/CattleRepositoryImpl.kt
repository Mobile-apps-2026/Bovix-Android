package pe.edu.upc.bovix.cattle.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import pe.edu.upc.bovix.cattle.data.local.AnimalDao
import pe.edu.upc.bovix.cattle.data.mapper.toDomain
import pe.edu.upc.bovix.cattle.data.mapper.toEntity
import pe.edu.upc.bovix.cattle.data.remote.CattleApi
import pe.edu.upc.bovix.cattle.data.remote.dto.CreateStableDto
import pe.edu.upc.bovix.cattle.domain.model.Animal
import pe.edu.upc.bovix.cattle.domain.model.AnimalGender
import pe.edu.upc.bovix.cattle.domain.model.AnimalStatus
import pe.edu.upc.bovix.cattle.domain.model.CattleData
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

    // Mapa nombre de establo → id en backend; se pobla en cada getCattleData()
    private val stableIdMap = mutableMapOf<String, Int>()

    override fun getCattleData(): Flow<Resource<CattleData>> = flow {
        emit(Resource.Loading)

        val cached = dao.getAll()
        if (cached.isNotEmpty()) {
            val cachedLots = cached.map { it.lot }.distinct().sorted()
            emit(Resource.Success(CattleData(cached.map { it.toDomain() }, cachedLots)))
        }

        try {
            val stables = api.getStables()
            stableIdMap.clear()
            stables.forEach { stableIdMap[it.name] = it.id }

            val bovines = api.getBovines()
            dao.upsertAll(bovines.map { it.toEntity() })

            val animals = bovines.map { it.toDomain() }
            val lots = stables.map { it.name }.sorted()
            emit(Resource.Success(CattleData(animals, lots)))
        } catch (io: IOException) {
            if (cached.isEmpty()) emit(Resource.Error("Sin conexión", io))
        } catch (e: Exception) {
            if (cached.isEmpty()) emit(Resource.Error(e.localizedMessage ?: "Error al cargar ganado", e))
        }
    }

    override suspend fun addAnimal(
        name: String, lot: String, status: AnimalStatus, gender: AnimalGender, weightKg: Int
    ): Animal {
        val stableId = stableIdMap[lot]
        val parts = buildBovineParts(name, gender.name, lot, status.name, weightKg, stableId)
        return api.createBovine(parts).toDomain()
    }

    override suspend fun updateAnimal(animal: Animal) {
        val id = animal.id.toIntOrNull() ?: return
        val stableId = stableIdMap[animal.lot]
        val parts = buildBovineParts(animal.name, animal.gender.name, animal.lot, animal.status.name, animal.weightKg, stableId)
        api.updateBovine(id, parts)
    }

    override suspend fun deleteAnimal(id: String) {
        api.deleteBovine(id.toInt())
    }

    override suspend fun addLot(name: String) {
        val stable = api.createStable(CreateStableDto(name = name, limit = 100))
        stableIdMap[stable.name] = stable.id
    }

    override suspend fun deleteLot(name: String) {
        val stableId = stableIdMap[name] ?: return
        api.deleteStable(stableId)
        stableIdMap.remove(name)
    }

    private fun buildBovineParts(
        name: String, gender: String, lot: String, status: String, weightKg: Int, stableId: Int?
    ): Map<String, okhttp3.RequestBody> {
        val plain = "text/plain".toMediaType()
        return buildMap {
            put("Name", name.toRequestBody(plain))
            put("Gender", gender.toRequestBody(plain))
            put("Lot", lot.toRequestBody(plain))
            put("Status", status.toRequestBody(plain))
            put("WeightKg", weightKg.toString().toRequestBody(plain))
            stableId?.let { put("StableId", it.toString().toRequestBody(plain)) }
        }
    }
}
