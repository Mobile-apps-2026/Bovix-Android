package pe.edu.upc.bovix.auth.presentation.login

import pe.edu.upc.bovix.auth.domain.model.User

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedUser: User? = null
)
