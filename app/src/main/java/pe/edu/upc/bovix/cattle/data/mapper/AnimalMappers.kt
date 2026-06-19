package pe.edu.upc.bovix.cattle.data.mapper

import pe.edu.upc.bovix.cattle.data.local.AnimalEntity
import pe.edu.upc.bovix.cattle.data.remote.dto.BovineDto
import pe.edu.upc.bovix.cattle.domain.model.Animal
import pe.edu.upc.bovix.cattle.domain.model.AnimalGender
import pe.edu.upc.bovix.cattle.domain.model.AnimalStatus

fun BovineDto.toDomain(): Animal = Animal(
    id = id.toString(),
    name = name,
    lot = lot ?: "",
    status = parseStatus(status),
    gender = parseGender(gender),
    weightKg = weightKg
)

fun BovineDto.toEntity(): AnimalEntity = AnimalEntity(
    id = id.toString(),
    name = name,
    lot = lot ?: "",
    status = status,
    gender = gender,
    weightKg = weightKg
)

fun AnimalEntity.toDomain(): Animal = Animal(
    id = id,
    name = name,
    lot = lot,
    status = parseStatus(status),
    gender = parseGender(gender),
    weightKg = weightKg
)

private fun parseStatus(raw: String): AnimalStatus =
    runCatching { AnimalStatus.valueOf(raw.uppercase()) }.getOrDefault(AnimalStatus.HEALTHY)

private fun parseGender(raw: String): AnimalGender =
    runCatching { AnimalGender.valueOf(raw.uppercase()) }.getOrDefault(AnimalGender.MALE)
