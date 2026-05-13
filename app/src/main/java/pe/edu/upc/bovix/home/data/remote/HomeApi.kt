package pe.edu.upc.bovix.home.data.remote

import pe.edu.upc.bovix.home.data.remote.dto.HomeResponseDto
import retrofit2.http.GET

interface HomeApi {
    @GET("home/summary")
    suspend fun getSummary(): HomeResponseDto
}
