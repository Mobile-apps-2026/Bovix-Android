package pe.edu.upc.bovix.cattle.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.bovix.cattle.domain.model.Animal
import pe.edu.upc.bovix.cattle.domain.model.AnimalGender
import pe.edu.upc.bovix.cattle.domain.model.AnimalStatus
import pe.edu.upc.bovix.cattle.domain.repository.CattleRepository
import pe.edu.upc.bovix.core.common.Resource
import javax.inject.Inject

@HiltViewModel
class CattleViewModel @Inject constructor(
    private val repository: CattleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CattleUiState())
    val uiState: StateFlow<CattleUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            repository.getCattleData().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, animals = result.data.animals, lots = result.data.lots, errorMessage = null)
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onLotSelected(lot: String) = _uiState.update { it.copy(selectedLot = lot) }
    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query) }

    // --- Animales ---

    fun showAddAnimalDialog() = _uiState.update { it.copy(showAddAnimalDialog = true) }
    fun hideAddAnimalDialog() = _uiState.update { it.copy(showAddAnimalDialog = false) }

    fun addAnimal(name: String, lot: String, status: AnimalStatus, gender: AnimalGender, weightKg: Int) {
        _uiState.update { it.copy(showAddAnimalDialog = false, isLoading = true) }
        launchCrud("Animal agregado") { repository.addAnimal(name.trim(), lot, status, gender, weightKg) }
    }

    fun showEditAnimalDialog(animal: Animal) = _uiState.update { it.copy(editingAnimal = animal) }
    fun hideEditAnimalDialog() = _uiState.update { it.copy(editingAnimal = null) }

    fun updateAnimal(updated: Animal) {
        _uiState.update { it.copy(editingAnimal = null, isLoading = true) }
        launchCrud("Cambios guardados") { repository.updateAnimal(updated) }
    }

    fun requestDeleteAnimal(animal: Animal) = _uiState.update { it.copy(deletingAnimal = animal) }
    fun cancelDeleteAnimal() = _uiState.update { it.copy(deletingAnimal = null) }

    fun confirmDeleteAnimal() {
        val target = _uiState.value.deletingAnimal ?: return
        _uiState.update { it.copy(deletingAnimal = null, isLoading = true) }
        launchCrud("Animal eliminado") { repository.deleteAnimal(target.id) }
    }

    // --- Lotes ---

    fun showAddLotDialog() = _uiState.update { it.copy(showAddLotDialog = true) }
    fun hideAddLotDialog() = _uiState.update { it.copy(showAddLotDialog = false) }

    fun addLot(name: String) {
        val clean = name.trim().uppercase()
        if (clean.isEmpty()) return
        _uiState.update { it.copy(showAddLotDialog = false, isLoading = true) }
        launchCrud("Lote $clean creado") { repository.addLot(clean) }
    }

    fun requestDeleteLot(lot: String) = _uiState.update { it.copy(deletingLot = lot) }
    fun cancelDeleteLot() = _uiState.update { it.copy(deletingLot = null) }

    fun confirmDeleteLot() {
        val lot = _uiState.value.deletingLot ?: return
        _uiState.update { it.copy(deletingLot = null, isLoading = true) }
        launchCrud("Lote eliminado") {
            repository.deleteLot(lot)
            _uiState.update { it.copy(selectedLot = "Todos") }
        }
    }

    fun consumeSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    private fun launchCrud(successMsg: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                _uiState.update { it.copy(snackbarMessage = successMsg) }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error") }
            }
        }
    }
}
