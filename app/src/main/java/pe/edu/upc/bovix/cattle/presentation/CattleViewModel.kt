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
import pe.edu.upc.bovix.cattle.domain.usecase.GetAnimalsUseCase
import pe.edu.upc.bovix.core.common.Resource
import javax.inject.Inject

@HiltViewModel
class CattleViewModel @Inject constructor(
    private val getAnimalsUseCase: GetAnimalsUseCase,
    private val repository: CattleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CattleUiState())
    val uiState: StateFlow<CattleUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            getAnimalsUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            animals = result.data.animals,
                            lots = result.data.lots,
                            errorMessage = null
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
    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query) }

    // --- Animales ---

    fun showAddAnimalDialog() = _uiState.update { it.copy(showAddAnimalDialog = true) }
    fun hideAddAnimalDialog() = _uiState.update { it.copy(showAddAnimalDialog = false) }

    fun addAnimal(name: String, lot: String, status: AnimalStatus, gender: AnimalGender, weightKg: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(showAddAnimalDialog = false, isLoading = true) }
            try {
                repository.addAnimal(name.trim(), lot, status, gender, weightKg)
                _uiState.update { it.copy(snackbarMessage = "Animal agregado") }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error al agregar animal") }
            }
        }
    }

    fun showEditAnimalDialog(animal: Animal) = _uiState.update { it.copy(editingAnimal = animal) }
    fun hideEditAnimalDialog() = _uiState.update { it.copy(editingAnimal = null) }

    fun updateAnimal(updated: Animal) {
        viewModelScope.launch {
            _uiState.update { it.copy(editingAnimal = null, isLoading = true) }
            try {
                repository.updateAnimal(updated)
                _uiState.update { it.copy(snackbarMessage = "Cambios guardados") }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error al actualizar animal") }
            }
        }
    }

    fun requestDeleteAnimal(animal: Animal) = _uiState.update { it.copy(deletingAnimal = animal) }
    fun cancelDeleteAnimal() = _uiState.update { it.copy(deletingAnimal = null) }

    fun confirmDeleteAnimal() {
        val target = _uiState.value.deletingAnimal ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(deletingAnimal = null, isLoading = true) }
            try {
                repository.deleteAnimal(target.id)
                _uiState.update { it.copy(snackbarMessage = "Animal eliminado") }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error al eliminar animal") }
            }
        }
    }

    // --- Lotes ---

    fun showAddLotDialog() = _uiState.update { it.copy(showAddLotDialog = true) }
    fun hideAddLotDialog() = _uiState.update { it.copy(showAddLotDialog = false) }

    fun addLot(name: String) {
        val clean = name.trim().uppercase()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(showAddLotDialog = false, isLoading = true) }
            try {
                repository.addLot(clean)
                _uiState.update { it.copy(snackbarMessage = "Lote $clean creado") }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error al crear lote") }
            }
        }
    }

    fun requestDeleteLot(lot: String) = _uiState.update { it.copy(deletingLot = lot) }
    fun cancelDeleteLot() = _uiState.update { it.copy(deletingLot = null) }

    fun confirmDeleteLot() {
        val lot = _uiState.value.deletingLot ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(deletingLot = null, isLoading = true) }
            try {
                repository.deleteLot(lot)
                _uiState.update { it.copy(snackbarMessage = "Lote eliminado", selectedLot = "Todos") }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error al eliminar lote") }
            }
        }
    }

    fun consumeSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
}
