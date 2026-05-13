package pe.edu.upc.bovix.auth.data.mapper

import pe.edu.upc.bovix.auth.data.local.UserEntity
import pe.edu.upc.bovix.auth.data.remote.dto.LoginResponseDto
import pe.edu.upc.bovix.auth.domain.model.User

// DTO -> Domain
fun LoginResponseDto.toDomain(): User = User(
    id = id,
    fullName = fullName,
    email = email,
    token = token
)

// DTO -> Entity (para cachear el usuario tras un login)
fun LoginResponseDto.toEntity(): UserEntity = UserEntity(
    id = id,
    fullName = fullName,
    email = email,
    token = token
)

// Entity -> Domain
fun UserEntity.toDomain(): User = User(
    id = id,
    fullName = fullName,
    email = email,
    token = token
)
