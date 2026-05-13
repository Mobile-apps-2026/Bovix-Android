package pe.edu.upc.bovix.cattle.data.mapper

import pe.edu.upc.bovix.cattle.data.local.AnimalEntity
import pe.edu.upc.bovix.cattle.data.remote.dto.AnimalDto
import pe.edu.upc.bovix.cattle.domain.model.Animal
import pe.edu.upc.bovix.cattle.domain.model.AnimalGender
import pe.edu.upc.bovix.cattle.domain.model.AnimalStatus

// DTO -> Domain
fun AnimalDto.toDomain(): Animal = Animal(
    id = id,
    name = name,
    lot = lot,
    status = parseStatus(status),
    gender = parseGender(gender),
    weightKg = weightKg
)

// DTO -> Entity
fun AnimalDto.toEntity(): AnimalEntity = AnimalEntity(
    id = id,
    name = name,
    lot = lot,
    status = status,
    gender = gender,
    weightKg = weightKg
)

// Entity -> Domain
fun AnimalEntity.toDomain(): Animal = Animal(
    id = id,
    name = name,
    lot = lot,
    status = parseStatus(status),
    gender = parseGender(gender),
    weightKg = weightKg
)

private fun parseStatus(raw: String): AnimalStatus =
    runCatching { AnimalStatus.valueOf(raw) }.getOrDefault(AnimalStatus.HEALTHY)

private fun parseGender(raw: String): AnimalGender =
    runCatching { AnimalGender.valueOf(raw) }.getOrDefault(AnimalGender.MALE)
