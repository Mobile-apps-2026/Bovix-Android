package pe.edu.upc.bovix.health.presentation

import pe.edu.upc.bovix.health.domain.model.HealthSummary

data class HealthUiState(
    val isLoading: Boolean = false,
    val data: HealthSummary? = null,
    val errorMessage: String? = null,
    val showScheduleDialog: Boolean = false,
    val confirmCancelAppointment: Boolean = false,
    val snackbarMessage: String? = null,
    val availableLots: List<String> = emptyList(),
    val availableVets: List<Pair<Int, String>> = emptyList(),
)
