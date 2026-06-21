package pe.edu.upc.bovix.feed.data.mapper

import pe.edu.upc.bovix.feed.data.local.FeedingComponentEntity
import pe.edu.upc.bovix.feed.data.local.FeedingPlanEntity
import pe.edu.upc.bovix.feed.data.remote.dto.FeedingComponentDto
import pe.edu.upc.bovix.feed.data.remote.dto.FeedingPlanDto
import pe.edu.upc.bovix.feed.domain.model.ComponentColor
import pe.edu.upc.bovix.feed.domain.model.FeedingComponent
import pe.edu.upc.bovix.feed.domain.model.FeedingPlan

private val COMPONENT_COLORS = ComponentColor.values()

/* ========== DTO -> Domain ========== */

fun FeedingPlanDto.toDomain(): FeedingPlan = FeedingPlan(
    id = id.toString(),
    lot = lot,
    dailyRationKg = dailyRationKg,
    animalCount = animalCount,
    components = components.mapIndexed { i, c -> c.toDomain(i) }
)

fun FeedingComponentDto.toDomain(index: Int = 0): FeedingComponent = FeedingComponent(
    id = id.toString(),
    name = name,
    percentage = percentage,
    amountKg = amountKg,
    color = COMPONENT_COLORS[index % COMPONENT_COLORS.size]
)

/* ========== DTO -> Entity ========== */

fun FeedingPlanDto.toEntity(): FeedingPlanEntity = FeedingPlanEntity(
    id = id.toString(), lot = lot, dailyRationKg = dailyRationKg, animalCount = animalCount
)

fun FeedingComponentDto.toEntity(planId: String, index: Int = 0): FeedingComponentEntity =
    FeedingComponentEntity(
        id = id.toString(), planId = planId, name = name,
        percentage = percentage, amountKg = amountKg,
        color = COMPONENT_COLORS[index % COMPONENT_COLORS.size].name
    )

/* ========== Entity -> Domain ========== */

fun FeedingPlanEntity.toDomain(components: List<FeedingComponentEntity>): FeedingPlan = FeedingPlan(
    id = id,
    lot = lot,
    dailyRationKg = dailyRationKg,
    animalCount = animalCount,
    components = components.mapIndexed { i, c -> c.toDomain(i) }
)

fun FeedingComponentEntity.toDomain(index: Int = 0): FeedingComponent = FeedingComponent(
    id = id,
    name = name,
    percentage = percentage,
    amountKg = amountKg,
    color = runCatching { ComponentColor.valueOf(color) }.getOrElse {
        COMPONENT_COLORS[index % COMPONENT_COLORS.size]
    }
)
