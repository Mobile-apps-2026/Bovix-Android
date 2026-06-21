package pe.edu.upc.bovix.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.bovix.auth.domain.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChange(v: String) = _uiState.update { it.copy(fullName = v, errorMessage = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, errorMessage = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, errorMessage = null) }

    fun onRegisterClicked() {
        val s = _uiState.value
        val error = when {
            s.fullName.isBlank() -> "Ingresa tu nombre completo"
            s.email.isBlank() || !s.email.contains('@') -> "Ingresa un correo válido"
            s.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            s.password != s.confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.register(s.fullName.trim(), s.email.trim(), s.password)
                _uiState.update { it.copy(isLoading = false, registeredSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "No se pudo crear la cuenta")
                }
            }
        }
    }

    fun consumeSuccess() = _uiState.update { it.copy(registeredSuccess = false) }
}
