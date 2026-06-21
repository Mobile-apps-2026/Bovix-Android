package pe.edu.upc.bovix.cattle.domain.model

data class Animal(
    val id: String,
    val name: String,
    val lot: String,
    val status: AnimalStatus,
    val gender: AnimalGender,
    val weightKg: Int
)

enum class AnimalStatus(val label: String) {
    HEALTHY("Saludable"),
    MONITORED("En seguimiento"),
    QUARANTINE("En cuarentena"),
    DECEASED("Fallecido")
}

enum class AnimalGender { MALE, FEMALE }
