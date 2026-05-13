package pe.edu.upc.bovix.auth.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import pe.edu.upc.bovix.auth.domain.model.User
import pe.edu.upc.bovix.auth.domain.repository.AuthRepository
import pe.edu.upc.bovix.core.common.Resource
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<User>> {
        if (email.isBlank() || password.isBlank()) {
            return flowOf(Resource.Error("Correo y contraseña son obligatorios"))
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return flowOf(Resource.Error("Correo electrónico inválido"))
        }
        return repository.login(email.trim(), password)
    }
}
