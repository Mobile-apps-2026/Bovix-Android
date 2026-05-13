package pe.edu.upc.bovix.home.domain.model

data class HomeData(
    val userName: String,
    val stats: HomeStats,
    val alert: HomeAlert?,
    val activities: List<HomeActivity>
)

data class HomeStats(
    val totalAnimals: Int,
    val activeLots: Int,
    val appointmentsToday: Int,
    val alerts: Int
)

data class HomeAlert(
    val title: String,
    val description: String
)

data class HomeActivity(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: HomeActivityType
)

enum class HomeActivityType { REGISTRATION, FEED_UPDATE, VET_VISIT }
