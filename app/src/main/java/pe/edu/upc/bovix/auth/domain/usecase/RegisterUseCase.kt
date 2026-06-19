package pe.edu.upc.bovix.auth.domain.usecase

import pe.edu.upc.bovix.auth.domain.model.User
import pe.edu.upc.bovix.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(fullName: String, email: String, password: String): User =
        repository.register(fullName, email, password)
}
