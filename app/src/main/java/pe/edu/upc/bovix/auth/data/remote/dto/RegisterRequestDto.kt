package pe.edu.upc.bovix.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String = "FARMER"
)
