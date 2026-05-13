package pe.edu.upc.bovix.cattle.presentation

import pe.edu.upc.bovix.cattle.domain.model.Animal

data class CattleUiState(
    val isLoading: Boolean = false,
    val animals: List<Animal> = emptyList(),
    val selectedLot: String = "Todos",   // "Todos", "Lote A", "Lote B", ...
    val query: String = "",
    val errorMessage: String? = null
) {
    val availableLots: List<String>
        get() = listOf("Todos") + animals.map { "Lote ${it.lot}" }.distinct().sorted()

    val filteredAnimals: List<Animal>
        get() = animals.filter { animal ->
            val matchesLot = selectedLot == "Todos" || "Lote ${animal.lot}" == selectedLot
            val q = query.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                animal.name.lowercase().contains(q) ||
                animal.id.lowercase().contains(q) ||
                animal.lot.lowercase().contains(q)
            matchesLot && matchesQuery
        }
}
