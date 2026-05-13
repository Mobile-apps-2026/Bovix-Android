package pe.edu.upc.bovix.cattle.data.remote

import pe.edu.upc.bovix.cattle.data.remote.dto.AnimalDto
import retrofit2.http.GET

interface CattleApi {
    @GET("cattle")
    suspend fun getAnimals(): List<AnimalDto>
}
