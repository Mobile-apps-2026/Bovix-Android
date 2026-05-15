package pe.edu.upc.bovix.feed.data.remote

import pe.edu.upc.bovix.feed.data.remote.dto.FeedingPlanDto
import retrofit2.http.GET

interface FeedingApi {
    @GET("feeding-plans")
    suspend fun getFeedingPlans(): List<FeedingPlanDto>
}
