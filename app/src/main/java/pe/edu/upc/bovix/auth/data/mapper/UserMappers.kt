package pe.edu.upc.bovix.auth.data.mapper

import android.util.Base64
import org.json.JSONObject
import pe.edu.upc.bovix.auth.data.local.UserEntity
import pe.edu.upc.bovix.auth.data.remote.dto.LoginResponseDto
import pe.edu.upc.bovix.auth.domain.model.User

// ClaimTypes.Name/.Sid en .NET se mapean a estas URIs en el JWT
private const val CLAIM_NAME = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"
private const val CLAIM_SID  = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/sid"

fun decodeJwt(token: String): Pair<String, String> {
    return try {
        val payload = token.split(".").getOrNull(1) ?: return "0" to "Usuario"
        val decoded = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
        val json = JSONObject(decoded)
        val id = json.optString(CLAIM_SID, "0")
        val name = json.optString(CLAIM_NAME, "Usuario")
        id to name
    } catch (_: Exception) {
        "0" to "Usuario"
    }
}

fun LoginResponseDto.toUser(email: String, displayName: String?): User {
    val (id, username) = decodeJwt(token)
    return User(id = id, fullName = displayName ?: username, email = email, token = token)
}

fun LoginResponseDto.toEntity(email: String, displayName: String?): UserEntity {
    val (id, username) = decodeJwt(token)
    return UserEntity(id = id, fullName = displayName ?: username, email = email, token = token)
}

fun UserEntity.toDomain(): User = User(id = id, fullName = fullName, email = email, token = token)
