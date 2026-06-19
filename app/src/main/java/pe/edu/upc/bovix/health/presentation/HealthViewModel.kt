package pe.edu.upc.bovix.health.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.bovix.cattle.domain.usecase.GetAnimalsUseCase
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.health.domain.model.HealthSummary
import pe.edu.upc.bovix.health.domain.repository.HealthRepository
import pe.edu.upc.bovix.health.domain.usecase.GetHealthSummaryUseCase
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val getHealthSummaryUseCase: GetHealthSummaryUseCase,
    private val getAnimalsUseCase: GetAnimalsUseCase,
    private val repository: HealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init {
        load()
        loadAvailableLots()
    }

    private fun loadAvailableLots() {
        viewModelScope.launch {
            getAnimalsUseCase().collect { result ->
                if (result is Resource.Success) {
                    _uiState.update { it.copy(availableLots = result.data.lots) }
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            getHealthSummaryUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, data = result.data, errorMessage = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    // --- Citas ---

    fun showScheduleDialog() = _uiState.update { it.copy(showScheduleDialog = true) }
    fun hideScheduleDialog() = _uiState.update { it.copy(showScheduleDialog = false) }

    fun scheduleAppointment(veterinarianName: String, lots: String, scheduledAt: LocalDateTime) {
        viewModelScope.launch {
            _uiState.update { it.copy(showScheduleDialog = false, isLoading = true) }
            try {
                repository.scheduleAppointment(veterinarianName.trim(), lots.trim().ifBlank { null }, scheduledAt)
                _uiState.update { it.copy(snackbarMessage = "Cita agendada") }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error al agendar cita") }
            }
        }
    }

    fun requestCancelAppointment() = _uiState.update { it.copy(confirmCancelAppointment = true) }
    fun dismissCancelAppointment() = _uiState.update { it.copy(confirmCancelAppointment = false) }

    fun confirmCancelAppointment() {
        val id = _uiState.value.data?.nextAppointment?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(confirmCancelAppointment = false, isLoading = true) }
            try {
                repository.cancelAppointment(id)
                _uiState.update { state ->
                    state.copy(
                        data = state.data?.copy(nextAppointment = null),
                        snackbarMessage = "Cita cancelada"
                    )
                }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error al cancelar cita") }
            }
        }
    }

    fun consumeSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
}
