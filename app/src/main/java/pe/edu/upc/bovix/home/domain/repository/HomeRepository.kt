package pe.edu.upc.bovix.home.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.home.domain.model.HomeData

interface HomeRepository {
    fun getHomeData(): Flow<Resource<HomeData>>
}
