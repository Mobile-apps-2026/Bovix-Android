package pe.edu.upc.bovix.home.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HomeResponseDto(
    @SerializedName("userName") val userName: String,
    @SerializedName("stats") val stats: HomeStatsDto,
    @SerializedName("alert") val alert: HomeAlertDto?,
    @SerializedName("activities") val activities: List<HomeActivityDto>
)

data class HomeStatsDto(
    @SerializedName("totalAnimals") val totalAnimals: Int,
    @SerializedName("activeLots") val activeLots: Int,
    @SerializedName("appointmentsToday") val appointmentsToday: Int,
    @SerializedName("alerts") val alerts: Int
)

data class HomeAlertDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String
)

data class HomeActivityDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("type") val type: String
)
