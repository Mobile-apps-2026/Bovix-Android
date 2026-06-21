package pe.edu.upc.bovix.core.common

// Wrapper genérico para estados asíncronos (Loading, Success, Error).
// Los ViewModels colectan este flow y actualizan su UiState en consecuencia.
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
}
