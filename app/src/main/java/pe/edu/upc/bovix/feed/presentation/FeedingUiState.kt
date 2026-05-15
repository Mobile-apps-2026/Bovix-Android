package pe.edu.upc.bovix.feed.presentation

import pe.edu.upc.bovix.feed.domain.model.FeedingPlan

data class FeedingUiState(
    val isLoading: Boolean = false,
    val plans: List<FeedingPlan> = emptyList(),
    val selectedLot: String? = null,    // null hasta que cargue
    val errorMessage: String? = null
) {
    /** Plan que se está mostrando actualmente */
    val selectedPlan: FeedingPlan?
        get() = plans.firstOrNull { it.lot == selectedLot } ?: plans.firstOrNull()
}
