package pe.edu.upc.bovix.feed.data.remote

import pe.edu.upc.bovix.feed.data.remote.dto.AddFeedingComponentDto
import pe.edu.upc.bovix.feed.data.remote.dto.CreateFeedingPlanDto
import pe.edu.upc.bovix.feed.data.remote.dto.FeedingComponentDto
import pe.edu.upc.bovix.feed.data.remote.dto.FeedingPlanDto
import pe.edu.upc.bovix.feed.data.remote.dto.UpdateFeedingComponentDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FeedingApi {
    @GET("feeding-plans")
    suspend fun getFeedingPlans(): List<FeedingPlanDto>

    @POST("feeding-plans")
    suspend fun createFeedingPlan(@Body body: CreateFeedingPlanDto): FeedingPlanDto

    @DELETE("feeding-plans/{id}")
    suspend fun deleteFeedingPlan(@Path("id") id: Int)

    @POST("feeding-plans/{id}/components")
    suspend fun addComponent(
        @Path("id") planId: Int,
        @Body body: AddFeedingComponentDto
    ): FeedingComponentDto

    @PUT("feeding-plans/{id}/components/{componentId}")
    suspend fun updateComponent(
        @Path("id") planId: Int,
        @Path("componentId") componentId: Int,
        @Body body: UpdateFeedingComponentDto
    ): FeedingComponentDto

    @DELETE("feeding-plans/{id}/components/{componentId}")
    suspend fun deleteComponent(
        @Path("id") planId: Int,
        @Path("componentId") componentId: Int
    )
}
