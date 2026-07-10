package pe.edu.upc.bovix.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

// El backend solo devuelve el token; la info del usuario se extrae del JWT.
data class LoginResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("role") val role: String = "FARMER"
)
