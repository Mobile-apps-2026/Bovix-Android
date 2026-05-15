package pe.edu.upc.bovix.feed.domain.model

/**
 * Plan alimentario por lote.
 * - dailyRationKg: ración diaria por animal
 * - animalCount: animales en el lote (para calcular total)
 * - components: composición del plan (heno, maíz, etc.)
 */
data class FeedingPlan(
    val id: String,
    val lot: String,                // ej. "A"
    val dailyRationKg: Double,      // ración por animal/día
    val animalCount: Int,
    val components: List<FeedingComponent>
) {
    /** Total diario del lote = ración × animales */
    val totalDailyKg: Double get() = dailyRationKg * animalCount
}

data class FeedingComponent(
    val id: String,
    val name: String,               // ej. "Heno de alfalfa"
    val percentage: Int,            // 0..100
    val amountKg: Double,           // kg de este componente / animal / día
    val color: ComponentColor
)

/**
 * Color semántico de cada componente (para la barra de progreso).
 * El layer de presentación mapea esto a un Color real.
 */
enum class ComponentColor { MINT, AMBER, GREEN, SKY }
