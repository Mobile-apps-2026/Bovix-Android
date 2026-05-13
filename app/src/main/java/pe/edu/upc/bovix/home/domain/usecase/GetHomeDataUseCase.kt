package pe.edu.upc.bovix.home.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.home.domain.model.HomeData
import pe.edu.upc.bovix.home.domain.repository.HomeRepository
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    operator fun invoke(): Flow<Resource<HomeData>> = repository.getHomeData()
}
