package pe.edu.upc.bovix.auth.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upc.bovix.auth.data.local.UserDao
import pe.edu.upc.bovix.auth.data.mapper.toDomain
import pe.edu.upc.bovix.auth.data.mapper.toEntity
import pe.edu.upc.bovix.auth.data.remote.AuthApi
import pe.edu.upc.bovix.auth.data.remote.dto.LoginRequestDto
import pe.edu.upc.bovix.auth.domain.model.User
import pe.edu.upc.bovix.auth.domain.repository.AuthRepository
import pe.edu.upc.bovix.core.common.Resource
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val dao: UserDao
) : AuthRepository {

    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            // Credenciales fijas de demo; quitar este bloque cuando el backend esté disponible.
            if (email.equals("juan@ejemplo.com", ignoreCase = true) && password == "123456") {
                val dto = pe.edu.upc.bovix.auth.data.remote.dto.LoginResponseDto(
                    id = "u-001",
                    fullName = "Juan Quispe",
                    email = email,
                    token = "demo-token"
                )
                dao.upsert(dto.toEntity())
                emit(Resource.Success(dto.toDomain()))
                return@flow
            }

            val response = api.login(LoginRequestDto(email, password))
            dao.upsert(response.toEntity())
            emit(Resource.Success(response.toDomain()))
        } catch (io: IOException) {
            emit(Resource.Error("Sin conexión. Verifica tu red.", io))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Credenciales inválidas", e))
        }
    }

    override suspend fun getCachedUser(): User? = dao.getCurrent()?.toDomain()

    override suspend fun logout() {
        dao.clear()
    }
}
