package pe.edu.upc.bovix.home.presentation

import pe.edu.upc.bovix.home.domain.model.HomeData

data class HomeUiState(
    val isLoading: Boolean = false,
    val data: HomeData? = null,
    val errorMessage: String? = null,
    val userEmail: String = "",
    val showProfileSheet: Boolean = false,
    val loggedOut: Boolean = false,
)
