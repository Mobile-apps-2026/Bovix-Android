package pe.edu.upc.bovix.feed.data.mapper

import pe.edu.upc.bovix.feed.data.local.FeedingComponentEntity
import pe.edu.upc.bovix.feed.data.local.FeedingPlanEntity
import pe.edu.upc.bovix.feed.data.remote.dto.FeedingComponentDto
import pe.edu.upc.bovix.feed.data.remote.dto.FeedingPlanDto
import pe.edu.upc.bovix.feed.domain.model.ComponentColor
import pe.edu.upc.bovix.feed.domain.model.FeedingComponent
import pe.edu.upc.bovix.feed.domain.model.FeedingPlan

/* ========== DTO -> Domain ========== */

fun FeedingPlanDto.toDomain(): FeedingPlan = FeedingPlan(
    id = id,
    lot = lot,
    dailyRationKg = dailyRationKg,
    animalCount = animalCount,
    components = components.map { it.toDomain() }
)

fun FeedingComponentDto.toDomain(): FeedingComponent = FeedingComponent(
    id = id,
    name = name,
    percentage = percentage,
    amountKg = amountKg,
    color = parseColor(color)
)

/* ========== DTO -> Entity ========== */

fun FeedingPlanDto.toEntity(): FeedingPlanEntity = FeedingPlanEntity(
    id = id, lot = lot, dailyRationKg = dailyRationKg, animalCount = animalCount
)

fun FeedingComponentDto.toEntity(planId: String): FeedingComponentEntity =
    FeedingComponentEntity(
        id = id, planId = planId, name = name,
        percentage = percentage, amountKg = amountKg, color = color
    )

/* ========== Entity -> Domain ========== */

fun FeedingPlanEntity.toDomain(components: List<FeedingComponentEntity>): FeedingPlan = FeedingPlan(
    id = id,
    lot = lot,
    dailyRationKg = dailyRationKg,
    animalCount = animalCount,
    components = components.map { it.toDomain() }
)

fun FeedingComponentEntity.toDomain(): FeedingComponent = FeedingComponent(
    id = id,
    name = name,
    percentage = percentage,
    amountKg = amountKg,
    color = parseColor(color)
)

/* ========== Helpers ========== */

private fun parseColor(raw: String): ComponentColor =
    runCatching { ComponentColor.valueOf(raw.uppercase()) }
        .getOrDefault(ComponentColor.MINT)
