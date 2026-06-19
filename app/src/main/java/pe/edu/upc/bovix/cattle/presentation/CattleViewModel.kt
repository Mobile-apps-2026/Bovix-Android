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
import pe.edu.upc.bovix.cattle.domain.usecase.GetAnimalsUseCase
import pe.edu.upc.bovix.core.common.Resource
import javax.inject.Inject

@HiltViewModel
class CattleViewModel @Inject constructor(
    private val getAnimalsUseCase: GetAnimalsUseCase
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
                            animals = result.data,
                            lots = result.data.map { a -> a.lot }.distinct().sorted(),
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
        val id = "#${(System.currentTimeMillis() % 900 + 100)}"
        _uiState.update { state ->
            state.copy(
                animals = state.animals + Animal(id, name.trim(), lot, status, gender, weightKg),
                lots = (state.lots + lot).distinct().sorted(),
                showAddAnimalDialog = false,
                snackbarMessage = "Animal agregado"
            )
        }
    }

    fun showEditAnimalDialog(animal: Animal) = _uiState.update { it.copy(editingAnimal = animal) }
    fun hideEditAnimalDialog() = _uiState.update { it.copy(editingAnimal = null) }

    fun updateAnimal(updated: Animal) {
        _uiState.update { state ->
            state.copy(
                animals = state.animals.map { if (it.id == updated.id) updated else it },
                lots = (state.lots + updated.lot).distinct().sorted(),
                editingAnimal = null,
                snackbarMessage = "Cambios guardados"
            )
        }
    }

    fun requestDeleteAnimal(animal: Animal) = _uiState.update { it.copy(deletingAnimal = animal) }
    fun cancelDeleteAnimal() = _uiState.update { it.copy(deletingAnimal = null) }

    fun confirmDeleteAnimal() {
        val target = _uiState.value.deletingAnimal ?: return
        _uiState.update {
            it.copy(
                animals = it.animals.filter { a -> a.id != target.id },
                deletingAnimal = null,
                snackbarMessage = "Animal eliminado"
            )
        }
    }

    // --- Lotes ---

    fun showAddLotDialog() = _uiState.update { it.copy(showAddLotDialog = true) }
    fun hideAddLotDialog() = _uiState.update { it.copy(showAddLotDialog = false) }

    fun addLot(name: String) {
        val clean = name.trim().uppercase()
        if (clean.isEmpty()) return
        _uiState.update {
            it.copy(
                lots = (it.lots + clean).distinct().sorted(),
                showAddLotDialog = false,
                snackbarMessage = "Lote $clean creado"
            )
        }
    }

    fun requestDeleteLot(lot: String) = _uiState.update { it.copy(deletingLot = lot) }
    fun cancelDeleteLot() = _uiState.update { it.copy(deletingLot = null) }

    fun confirmDeleteLot() {
        val lot = _uiState.value.deletingLot ?: return
        _uiState.update {
            it.copy(
                lots = it.lots.filter { l -> l != lot },
                animals = it.animals.filter { a -> a.lot != lot },
                deletingLot = null,
                selectedLot = "Todos",
                snackbarMessage = "Lote eliminado"
            )
        }
    }

    fun consumeSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
}
