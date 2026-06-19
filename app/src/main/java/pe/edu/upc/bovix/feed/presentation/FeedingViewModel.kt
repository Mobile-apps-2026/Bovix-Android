package pe.edu.upc.bovix.feed.presentation

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
import pe.edu.upc.bovix.feed.domain.model.FeedingComponent
import pe.edu.upc.bovix.feed.domain.model.FeedingPlan
import pe.edu.upc.bovix.feed.domain.repository.FeedingRepository
import pe.edu.upc.bovix.feed.domain.usecase.GetFeedingPlansUseCase
import javax.inject.Inject

@HiltViewModel
class FeedingViewModel @Inject constructor(
    private val getFeedingPlansUseCase: GetFeedingPlansUseCase,
    private val getAnimalsUseCase: GetAnimalsUseCase,
    private val repository: FeedingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedingUiState())
    val uiState: StateFlow<FeedingUiState> = _uiState.asStateFlow()

    init {
        load()
        loadAvailableLots()
    }

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

    private fun loadAvailableLots() {
        viewModelScope.launch {
            getAnimalsUseCase().collect { result ->
                if (result is Resource.Success) {
                    val data = result.data
                    val lots = data.lots.map { lot ->
                        lot to data.animals.count { it.lot == lot }
                    }
                    _uiState.update { it.copy(availableLots = lots) }
                }
            }
        }
    }

    fun onLotSelected(lot: String) = _uiState.update { it.copy(selectedLot = lot) }

    // --- Editar plan existente ---

    fun startEditPlan(plan: FeedingPlan) = _uiState.update { it.copy(editingPlan = plan) }
    fun cancelEditPlan() = _uiState.update { it.copy(editingPlan = null) }

    fun savePlanEdit(lot: String, dailyRationKg: Double, animalCount: Int, components: List<FeedingComponent>) {
        val planId = _uiState.value.editingPlan?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(editingPlan = null, isLoading = true) }
            try {
                repository.savePlanEdit(planId, lot, dailyRationKg, animalCount, components)
                _uiState.update { it.copy(snackbarMessage = "Plan actualizado") }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error al actualizar plan") }
            }
        }
    }

    // --- Crear plan nuevo ---

    fun showCreatePlanDialog() = _uiState.update { it.copy(showCreatePlanDialog = true) }
    fun hideCreatePlanDialog() = _uiState.update { it.copy(showCreatePlanDialog = false) }

    fun createPlan(lot: String, dailyRationKg: Double, animalCount: Int, components: List<FeedingComponent>) {
        viewModelScope.launch {
            _uiState.update { it.copy(showCreatePlanDialog = false, isLoading = true) }
            try {
                val newPlan = repository.createPlan(lot.trim().uppercase(), dailyRationKg, animalCount, components)
                _uiState.update { it.copy(snackbarMessage = "Plan creado", selectedLot = newPlan.lot) }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error al crear plan") }
            }
        }
    }

    // --- Eliminar plan ---

    fun requestDeletePlan(plan: FeedingPlan) = _uiState.update { it.copy(deletingPlan = plan) }
    fun cancelDeletePlan() = _uiState.update { it.copy(deletingPlan = null) }

    fun confirmDeletePlan() {
        val target = _uiState.value.deletingPlan ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(deletingPlan = null, isLoading = true) }
            try {
                repository.deletePlan(target.id)
                _uiState.update { it.copy(snackbarMessage = "Plan eliminado") }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error al eliminar plan") }
            }
        }
    }

    fun consumeSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
}
