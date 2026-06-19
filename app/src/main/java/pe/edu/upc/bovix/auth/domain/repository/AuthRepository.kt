package pe.edu.upc.bovix.auth.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.auth.domain.model.User
import pe.edu.upc.bovix.core.common.Resource

// Contrato de autenticación; la implementación vive en la capa de data.
interface AuthRepository {
    fun login(email: String, password: String): Flow<Resource<User>>
    suspend fun register(fullName: String, email: String, password: String): User
    suspend fun getCachedUser(): User?
    suspend fun logout()
}
