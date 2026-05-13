package pe.edu.upc.bovix.home.data.mapper

import pe.edu.upc.bovix.home.data.local.HomeStatsEntity
import pe.edu.upc.bovix.home.data.remote.dto.HomeActivityDto
import pe.edu.upc.bovix.home.data.remote.dto.HomeResponseDto
import pe.edu.upc.bovix.home.domain.model.HomeActivity
import pe.edu.upc.bovix.home.domain.model.HomeActivityType
import pe.edu.upc.bovix.home.domain.model.HomeAlert
import pe.edu.upc.bovix.home.domain.model.HomeData
import pe.edu.upc.bovix.home.domain.model.HomeStats

// DTO -> Domain
fun HomeResponseDto.toDomain(): HomeData = HomeData(
    userName = userName,
    stats = HomeStats(
        totalAnimals = stats.totalAnimals,
        activeLots = stats.activeLots,
        appointmentsToday = stats.appointmentsToday,
        alerts = stats.alerts
    ),
    alert = alert?.let { HomeAlert(it.title, it.description) },
    activities = activities.map { it.toDomain() }
)

fun HomeActivityDto.toDomain(): HomeActivity = HomeActivity(
    id = id,
    title = title,
    subtitle = subtitle,
    type = runCatching { HomeActivityType.valueOf(type) }
        .getOrDefault(HomeActivityType.REGISTRATION)
)

// DTO -> Entity (solo cacheamos las stats principales)
fun HomeResponseDto.toEntity(): HomeStatsEntity = HomeStatsEntity(
    userName = userName,
    totalAnimals = stats.totalAnimals,
    activeLots = stats.activeLots,
    appointmentsToday = stats.appointmentsToday,
    alerts = stats.alerts,
    alertTitle = alert?.title,
    alertDescription = alert?.description
)

// Entity -> Domain (sin actividades, solo el "header")
fun HomeStatsEntity.toDomain(): HomeData = HomeData(
    userName = userName,
    stats = HomeStats(totalAnimals, activeLots, appointmentsToday, alerts),
    alert = if (alertTitle != null && alertDescription != null)
        HomeAlert(alertTitle, alertDescription) else null,
    activities = emptyList()
)
