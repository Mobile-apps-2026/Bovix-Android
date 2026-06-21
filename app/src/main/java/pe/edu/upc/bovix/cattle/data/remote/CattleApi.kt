package pe.edu.upc.bovix.cattle.data.remote

import okhttp3.RequestBody
import pe.edu.upc.bovix.cattle.data.remote.dto.BovineDto
import pe.edu.upc.bovix.cattle.data.remote.dto.CreateStableDto
import pe.edu.upc.bovix.cattle.data.remote.dto.StableDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

interface CattleApi {
    @GET("bovines")
    suspend fun getBovines(): List<BovineDto>

    @Multipart
    @POST("bovines")
    suspend fun createBovine(@PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>): BovineDto

    @Multipart
    @PUT("bovines/{id}")
    suspend fun updateBovine(
        @Path("id") id: Int,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>
    ): BovineDto

    @DELETE("bovines/{id}")
    suspend fun deleteBovine(@Path("id") id: Int)

    @GET("stables")
    suspend fun getStables(): List<StableDto>

    @POST("stables")
    suspend fun createStable(@Body body: CreateStableDto): StableDto

    @DELETE("stables/{id}")
    suspend fun deleteStable(@Path("id") id: Int)
}
