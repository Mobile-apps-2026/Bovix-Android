package pe.edu.upc.bovix.feed.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FeedingPlanDto(
    @SerializedName("id") val id: Int,
    @SerializedName("lot") val lot: String,
    @SerializedName("dailyRationKg") val dailyRationKg: Double,
    @SerializedName("animalCount") val animalCount: Int,
    @SerializedName("notes") val notes: String?,
    @SerializedName("components") val components: List<FeedingComponentDto>
)

data class FeedingComponentDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("percentage") val percentage: Int,
    @SerializedName("amountKg") val amountKg: Double
)

data class CreateFeedingPlanDto(
    @SerializedName("lot") val lot: String,
    @SerializedName("dailyRationKg") val dailyRationKg: Double,
    @SerializedName("animalCount") val animalCount: Int,
    @SerializedName("notes") val notes: String? = null
)

data class AddFeedingComponentDto(
    @SerializedName("name") val name: String,
    @SerializedName("percentage") val percentage: Int,
    @SerializedName("amountKg") val amountKg: Double
)

data class UpdateFeedingComponentDto(
    @SerializedName("name") val name: String,
    @SerializedName("percentage") val percentage: Int,
    @SerializedName("amountKg") val amountKg: Double
)
