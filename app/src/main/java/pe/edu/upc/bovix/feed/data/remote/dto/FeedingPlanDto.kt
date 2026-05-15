package pe.edu.upc.bovix.feed.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FeedingPlanDto(
    @SerializedName("id") val id: String,
    @SerializedName("lot") val lot: String,
    @SerializedName("dailyRationKg") val dailyRationKg: Double,
    @SerializedName("animalCount") val animalCount: Int,
    @SerializedName("components") val components: List<FeedingComponentDto>
)

data class FeedingComponentDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("percentage") val percentage: Int,
    @SerializedName("amountKg") val amountKg: Double,
    @SerializedName("color") val color: String   // MINT/AMBER/GREEN/SKY
)
