package pe.edu.upc.bovix.cattle.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.cattle.domain.model.CattleData
import pe.edu.upc.bovix.cattle.domain.repository.CattleRepository
import pe.edu.upc.bovix.core.common.Resource
import javax.inject.Inject

class GetAnimalsUseCase @Inject constructor(
    private val repository: CattleRepository
) {
    operator fun invoke(): Flow<Resource<CattleData>> = repository.getCattleData()
}
