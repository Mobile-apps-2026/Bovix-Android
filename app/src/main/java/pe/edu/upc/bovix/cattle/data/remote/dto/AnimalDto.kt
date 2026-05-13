package pe.edu.upc.bovix.cattle.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AnimalDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("lot") val lot: String,
    @SerializedName("status") val status: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("weightKg") val weightKg: Int
)
