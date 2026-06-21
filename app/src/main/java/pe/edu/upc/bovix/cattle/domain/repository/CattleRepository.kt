package pe.edu.upc.bovix.cattle.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.cattle.domain.model.Animal
import pe.edu.upc.bovix.cattle.domain.model.AnimalGender
import pe.edu.upc.bovix.cattle.domain.model.AnimalStatus
import pe.edu.upc.bovix.cattle.domain.model.CattleData
import pe.edu.upc.bovix.core.common.Resource

interface CattleRepository {
    fun getCattleData(): Flow<Resource<CattleData>>
    suspend fun addAnimal(name: String, lot: String, status: AnimalStatus, gender: AnimalGender, weightKg: Int): Animal
    suspend fun updateAnimal(animal: Animal)
    suspend fun deleteAnimal(id: String)
    suspend fun addLot(name: String)
    suspend fun deleteLot(name: String)
}
