package pe.edu.upc.bovix.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.bovix.auth.domain.repository.AuthRepository
import pe.edu.upc.bovix.cattle.data.local.AnimalDao
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.home.domain.model.HomeData
import pe.edu.upc.bovix.home.domain.model.HomeStats
import pe.edu.upc.bovix.home.domain.repository.HomeRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val authRepository: AuthRepository,
    private val animalDao: AnimalDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
        observeAnimalStats()
        viewModelScope.launch {
            val user = authRepository.getCachedUser()
            _uiState.update { it.copy(userEmail = user?.email ?: "") }
        }
    }

    private fun observeAnimalStats() {
        viewModelScope.launch {
            animalDao.observeAll().collect { animals ->
                val totalAnimals = animals.size
                val activeLots = animals.map { it.lot }.filter { it.isNotBlank() }.distinct().size
                _uiState.update { state ->
                    val current = state.data
                    if (current != null) {
                        state.copy(data = current.copy(stats = current.stats.copy(totalAnimals = totalAnimals, activeLots = activeLots)))
                    } else if (totalAnimals > 0) {
                        state.copy(data = HomeData(state.userEmail, HomeStats(totalAnimals, activeLots, 0, 0), null, emptyList()))
                    } else state
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            homeRepository.getHomeData().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, data = result.data, errorMessage = null) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun showProfile() = _uiState.update { it.copy(showProfileSheet = true) }
    fun hideProfile() = _uiState.update { it.copy(showProfileSheet = false) }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(showProfileSheet = false, loggedOut = true) }
        }
    }

    fun consumeLogout() = _uiState.update { it.copy(loggedOut = false) }
}
