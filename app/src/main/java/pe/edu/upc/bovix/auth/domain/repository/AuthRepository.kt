package pe.edu.upc.bovix.auth.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.bovix.auth.domain.model.User
import pe.edu.upc.bovix.core.common.Resource

/**
 * Contrato definido en la capa de dominio.
 * La capa de data lo implementa (Inversión de dependencias).
 */
interface AuthRepository {
    fun login(email: String, password: String): Flow<Resource<User>>
    suspend fun getCachedUser(): User?
    suspend fun logout()
}
