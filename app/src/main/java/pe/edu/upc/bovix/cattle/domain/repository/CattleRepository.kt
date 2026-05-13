package pe.edu.upc.bovix.cattle.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.cattle.domain.model.Animal
import pe.edu.upc.bovix.core.common.Resource

interface CattleRepository {
    fun getAnimals(): Flow<Resource<List<Animal>>>
}
