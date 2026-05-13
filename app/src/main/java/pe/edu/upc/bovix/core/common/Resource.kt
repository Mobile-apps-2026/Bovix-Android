package pe.edu.upc.bovix.core.common

/**
 * Wrapper genérico para representar estados asíncronos.
 * Lo consumen los ViewModels para mapear hacia su UiState.
 */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
}
