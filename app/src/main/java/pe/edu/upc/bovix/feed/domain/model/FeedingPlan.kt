package pe.edu.upc.bovix.feed.domain.model

// dailyRationKg es la ración por animal; totalDailyKg se calcula multiplicando por animalCount.
data class FeedingPlan(
    val id: String,
    val lot: String,
    val dailyRationKg: Double,
    val animalCount: Int,
    val components: List<FeedingComponent>
) {
    val totalDailyKg: Double get() = dailyRationKg * animalCount
}

data class FeedingComponent(
    val id: String,
    val name: String,
    val percentage: Int,
    val amountKg: Double,
    val color: ComponentColor
)

// Color semántico del componente; la pantalla lo convierte a un Color de Compose.
enum class ComponentColor { MINT, AMBER, GREEN, SKY }
