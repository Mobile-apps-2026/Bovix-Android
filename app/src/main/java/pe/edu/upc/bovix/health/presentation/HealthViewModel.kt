package pe.edu.upc.bovix.health.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.health.domain.model.AlertSeverity
import pe.edu.upc.bovix.health.domain.model.AppointmentStatus
import pe.edu.upc.bovix.health.domain.model.ClinicalEntry
import pe.edu.upc.bovix.health.domain.model.HealthSummary
import pe.edu.upc.bovix.health.domain.model.VetAppointment
import pe.edu.upc.bovix.health.domain.usecase.GetHealthSummaryUseCase
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val getHealthSummaryUseCase: GetHealthSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init { load() }

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
        val appt = VetAppointment(
            id = "ap-${System.currentTimeMillis()}",
            veterinarianName = veterinarianName.trim(),
            scheduledAt = scheduledAt,
            lots = lots.trim(),
            status = AppointmentStatus.SCHEDULED
        )
        _uiState.update { state ->
            val current = state.data ?: HealthSummary(null, emptyList(), emptyList())
            state.copy(
                data = current.copy(nextAppointment = appt),
                showScheduleDialog = false,
                snackbarMessage = "Cita agendada"
            )
        }
    }

    fun requestCancelAppointment() = _uiState.update { it.copy(confirmCancelAppointment = true) }
    fun dismissCancelAppointment() = _uiState.update { it.copy(confirmCancelAppointment = false) }

    fun confirmCancelAppointment() {
        _uiState.update { state ->
            state.copy(
                data = state.data?.copy(nextAppointment = null),
                confirmCancelAppointment = false,
                snackbarMessage = "Cita cancelada"
            )
        }
    }

    // --- Vacunas ---

    fun markVaccineDone(id: String) {
        _uiState.update { state ->
            state.copy(
                data = state.data?.copy(
                    pendingVaccinations = state.data.pendingVaccinations.filter { it.id != id }
                ),
                snackbarMessage = "Vacuna marcada como aplicada"
            )
        }
    }

    // --- Historial clínico ---

    fun showAddClinicalDialog() = _uiState.update { it.copy(showAddClinicalDialog = true) }
    fun hideAddClinicalDialog() = _uiState.update { it.copy(showAddClinicalDialog = false) }

    fun addClinicalEntry(title: String, dateLabel: String, severity: AlertSeverity?) {
        val entry = ClinicalEntry(
            id = "cr-${System.currentTimeMillis()}",
            title = title.trim(),
            dateLabel = dateLabel.trim(),
            severity = severity
        )
        _uiState.update { state ->
            val current = state.data ?: HealthSummary(null, emptyList(), emptyList())
            // las entradas nuevas van al inicio
            state.copy(
                data = current.copy(clinicalHistory = listOf(entry) + current.clinicalHistory),
                showAddClinicalDialog = false,
                snackbarMessage = "Entrada clínica registrada"
            )
        }
    }

    fun consumeSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
}
