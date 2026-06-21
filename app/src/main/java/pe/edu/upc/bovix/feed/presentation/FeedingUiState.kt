package pe.edu.upc.bovix.feed.presentation

import pe.edu.upc.bovix.feed.domain.model.FeedingPlan

data class FeedingUiState(
    val isLoading: Boolean = false,
    val plans: List<FeedingPlan> = emptyList(),
    val selectedLot: String? = null,
    val errorMessage: String? = null,
    val editingPlan: FeedingPlan? = null,
    val showCreatePlanDialog: Boolean = false,
    val deletingPlan: FeedingPlan? = null,
    val snackbarMessage: String? = null,
    val availableLots: List<Pair<String, Int>> = emptyList(),
) {
    val selectedPlan: FeedingPlan?
        get() = plans.firstOrNull { it.lot == selectedLot } ?: plans.firstOrNull()
}
