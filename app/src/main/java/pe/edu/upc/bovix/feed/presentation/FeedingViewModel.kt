package pe.edu.upc.bovix.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.feed.domain.model.FeedingComponent
import pe.edu.upc.bovix.feed.domain.model.FeedingPlan
import pe.edu.upc.bovix.feed.domain.usecase.GetFeedingPlansUseCase
import javax.inject.Inject

@HiltViewModel
class FeedingViewModel @Inject constructor(
    private val getFeedingPlansUseCase: GetFeedingPlansUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedingUiState())
    val uiState: StateFlow<FeedingUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            getFeedingPlansUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            plans = result.data,
                            errorMessage = null,
                            selectedLot = it.selectedLot ?: result.data.firstOrNull()?.lot
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun onLotSelected(lot: String) = _uiState.update { it.copy(selectedLot = lot) }

    // --- Editar plan existente ---

    fun startEditPlan(plan: FeedingPlan) = _uiState.update { it.copy(editingPlan = plan) }
    fun cancelEditPlan() = _uiState.update { it.copy(editingPlan = null) }

    fun savePlanEdit(lot: String, dailyRationKg: Double, animalCount: Int, components: List<FeedingComponent>) {
        _uiState.update { state ->
            val updated = state.editingPlan?.copy(
                lot = lot,
                dailyRationKg = dailyRationKg,
                animalCount = animalCount,
                components = components
            ) ?: return@update state
            state.copy(
                plans = state.plans.map { if (it.id == updated.id) updated else it },
                editingPlan = null,
                snackbarMessage = "Plan actualizado"
            )
        }
    }

    // --- Crear plan nuevo ---

    fun showCreatePlanDialog() = _uiState.update { it.copy(showCreatePlanDialog = true) }
    fun hideCreatePlanDialog() = _uiState.update { it.copy(showCreatePlanDialog = false) }

    fun createPlan(lot: String, dailyRationKg: Double, animalCount: Int, components: List<FeedingComponent>) {
        val id = "plan-${lot.trim().uppercase()}-${System.currentTimeMillis()}"
        val newPlan = FeedingPlan(
            id = id,
            lot = lot.trim().uppercase(),
            dailyRationKg = dailyRationKg,
            animalCount = animalCount,
            components = components
        )
        _uiState.update { state ->
            val updatedPlans = (state.plans + newPlan).sortedBy { it.lot }
            state.copy(
                plans = updatedPlans,
                selectedLot = newPlan.lot,
                showCreatePlanDialog = false,
                snackbarMessage = "Plan creado"
            )
        }
    }

    // --- Eliminar plan ---

    fun requestDeletePlan(plan: FeedingPlan) = _uiState.update { it.copy(deletingPlan = plan) }
    fun cancelDeletePlan() = _uiState.update { it.copy(deletingPlan = null) }

    fun confirmDeletePlan() {
        val target = _uiState.value.deletingPlan ?: return
        _uiState.update { state ->
            val remaining = state.plans.filter { it.id != target.id }
            state.copy(
                plans = remaining,
                deletingPlan = null,
                selectedLot = remaining.firstOrNull()?.lot,
                snackbarMessage = "Plan eliminado"
            )
        }
    }

    fun consumeSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
}
