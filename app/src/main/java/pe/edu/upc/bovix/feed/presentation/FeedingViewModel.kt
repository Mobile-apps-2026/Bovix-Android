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
}
