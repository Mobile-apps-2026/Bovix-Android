package pe.edu.upc.bovix.auth.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upc.bovix.auth.data.local.UserDao
import pe.edu.upc.bovix.auth.data.mapper.toEntity
import pe.edu.upc.bovix.auth.data.mapper.toDomain
import pe.edu.upc.bovix.auth.data.remote.AuthApi
import pe.edu.upc.bovix.auth.data.remote.dto.LoginRequestDto
import pe.edu.upc.bovix.auth.data.remote.dto.RegisterRequestDto
import pe.edu.upc.bovix.auth.domain.model.User
import pe.edu.upc.bovix.auth.domain.repository.AuthRepository
import pe.edu.upc.bovix.cattle.data.local.AnimalDao
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.feed.data.local.FeedingDao
import pe.edu.upc.bovix.health.data.local.HealthDao
import pe.edu.upc.bovix.home.data.local.HomeStatsDao
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val userDao: UserDao,
    private val animalDao: AnimalDao,
    private val feedingDao: FeedingDao,
    private val healthDao: HealthDao,
    private val homeStatsDao: HomeStatsDao
) : AuthRepository {

    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.login(LoginRequestDto(email, password))
            val entity = response.toEntity(email, displayName = null)
            // Limpiar siempre antes de guardar la nueva sesión
            clearAllUserData()
            userDao.upsert(entity)
            emit(Resource.Success(entity.toDomain()))
        } catch (io: IOException) {
            emit(Resource.Error("Sin conexión. Verifica tu red.", io))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Credenciales inválidas", e))
        }
    }

    override suspend fun register(fullName: String, email: String, password: String): User {
        val response = api.register(
            RegisterRequestDto(
                username = fullName.trim(),
                password = password,
                email = email.trim().lowercase()
            )
        )
        val entity = response.toEntity(email.trim().lowercase(), displayName = fullName.trim())
        return entity.toDomain()
    }

    override suspend fun getCachedUser(): User? = userDao.getCurrent()?.toDomain()

    override suspend fun logout() {
        userDao.clear()
        clearAllUserData()
    }

    private suspend fun clearAllUserData() {
        animalDao.clear()
        feedingDao.clearPlans()
        feedingDao.clearComponents()
        healthDao.clearAppointments()
        healthDao.clearPendingVaccinations()
        healthDao.clearClinicalEntries()
        homeStatsDao.clear()
    }
}
