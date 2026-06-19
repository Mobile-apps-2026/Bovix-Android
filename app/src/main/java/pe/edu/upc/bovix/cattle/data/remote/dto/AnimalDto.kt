package pe.edu.upc.bovix.cattle.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BovineDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("lot") val lot: String?,
    @SerializedName("status") val status: String,
    @SerializedName("weightKg") val weightKg: Int,
    @SerializedName("stableId") val stableId: Int?
)

data class StableDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("limit") val limit: Int
)

data class CreateStableDto(
    @SerializedName("name") val name: String,
    @SerializedName("limit") val limit: Int
)
