package pe.edu.upc.bovix.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.bovix.auth.domain.repository.AuthRepository
import pe.edu.upc.bovix.cattle.data.local.AnimalDao
import pe.edu.upc.bovix.core.common.Resource
import pe.edu.upc.bovix.feed.data.local.FeedingDao
import pe.edu.upc.bovix.health.data.local.HealthDao
import pe.edu.upc.bovix.home.domain.model.HomeActivity
import pe.edu.upc.bovix.home.domain.model.HomeActivityType
import pe.edu.upc.bovix.home.domain.model.HomeData
import pe.edu.upc.bovix.home.domain.model.HomeStats
import pe.edu.upc.bovix.home.domain.repository.HomeRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val authRepository: AuthRepository,
    private val animalDao: AnimalDao,
    private val feedingDao: FeedingDao,
    private val healthDao: HealthDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
        observeLocalData()
        viewModelScope.launch {
            val user = authRepository.getCachedUser()
            _uiState.update { it.copy(userEmail = user?.email ?: "") }
        }
    }

    private fun observeLocalData() {
        viewModelScope.launch {
            combine(
                animalDao.observeAll(),
                feedingDao.observePlans(),
                healthDao.observeAppointments()
            ) { animals, plans, appointments ->
                val totalAnimals = animals.size
                val activeLots = animals.map { it.lot }.filter { it.isNotBlank() }.distinct().size

                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val appointmentsToday = appointments.count { it.scheduledAt.startsWith(today) }

                val activities = buildActivities(
                    animals.sortedByDescending { it.createdAt }.take(4),
                    plans.sortedByDescending { it.createdAt }.take(3)
                )

                Triple(HomeStats(totalAnimals, activeLots, appointmentsToday, 0), activities, appointmentsToday)
            }.collect { (stats, activities, _) ->
                _uiState.update { state ->
                    val current = state.data
                    if (current != null) {
                        state.copy(data = current.copy(
                            stats = current.stats.copy(
                                totalAnimals = stats.totalAnimals,
                                activeLots = stats.activeLots,
                                appointmentsToday = stats.appointmentsToday
                            ),
                            activities = activities.ifEmpty { current.activities }
                        ))
                    } else if (stats.totalAnimals > 0) {
                        state.copy(data = HomeData(state.userEmail, stats, null, activities))
                    } else state
                }
            }
        }
    }

    private fun buildActivities(
        animals: List<pe.edu.upc.bovix.cattle.data.local.AnimalEntity>,
        plans: List<pe.edu.upc.bovix.feed.data.local.FeedingPlanEntity>
    ): List<HomeActivity> {
        val items = mutableListOf<Pair<Long, HomeActivity>>()

        animals.forEach { animal ->
            items.add(animal.createdAt to HomeActivity(
                id = "animal_${animal.id}",
                title = "${animal.name} registrado en Lote ${animal.lot}",
                subtitle = relativeTime(animal.createdAt),
                type = HomeActivityType.REGISTRATION
            ))
        }

        plans.forEach { plan ->
            items.add(plan.createdAt to HomeActivity(
                id = "plan_${plan.id}",
                title = "Plan alimentario – Lote ${plan.lot}",
                subtitle = relativeTime(plan.createdAt),
                type = HomeActivityType.FEED_UPDATE
            ))
        }

        return items.sortedByDescending { it.first }.take(5).map { it.second }
    }

    private fun relativeTime(timestamp: Long): String {
        val diffMin = (System.currentTimeMillis() - timestamp) / 60_000
        return when {
            diffMin < 1    -> "Ahora mismo"
            diffMin < 60   -> "Hace ${diffMin}min"
            diffMin < 1440 -> "Hace ${diffMin / 60}h"
            else           -> "Hace ${diffMin / 1440}d"
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
