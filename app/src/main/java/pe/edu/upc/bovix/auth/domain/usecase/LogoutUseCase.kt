package pe.edu.upc.bovix.auth.domain.usecase

import pe.edu.upc.bovix.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() = repository.logout()
}
