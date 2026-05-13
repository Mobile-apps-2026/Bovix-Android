package pe.edu.upc.bovix.cattle.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.bovix.cattle.domain.model.Animal
import pe.edu.upc.bovix.cattle.domain.model.AnimalGender
import pe.edu.upc.bovix.cattle.domain.model.AnimalStatus
import pe.edu.upc.bovix.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CattleScreen(
    viewModel: CattleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(BgPrimary)) {

        // === Header ===
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(ForestGreen)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Text("Mi Ganado", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MintGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = ForestGreen, modifier = Modifier.size(18.dp))
            }
        }

        // === Search ===
        Surface(color = CardWhite, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text("Buscar animal o lote...", color = TextMute) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMute) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgPrimary,
                        unfocusedContainerColor = BgPrimary,
                        focusedBorderColor = MintGreen,
                        unfocusedBorderColor = BorderSoft
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // === Filtros por lote ===
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(state.availableLots) { lot ->
                val selected = state.selectedLot == lot
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.onLotSelected(lot) },
                    label = { Text(lot, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ForestGreen,
                        selectedLabelColor = Color.White,
                        containerColor = CardWhite,
                        labelColor = TextMid
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = BorderSoft,
                        selectedBorderColor = ForestGreen
                    )
                )
            }
        }

        // === Lista ===
        when {
            state.isLoading && state.animals.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = ForestGreen) }

            state.errorMessage != null && state.animals.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) { Text(state.errorMessage!!, color = Danger) }

            state.filteredAnimals.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) { Text("Sin resultados para el filtro actual.", color = TextMute) }

            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.filteredAnimals, key = { it.id }) { animal ->
                    AnimalCard(animal)
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun AnimalCard(animal: Animal) {
    val statusColor = when (animal.status) {
        AnimalStatus.HEALTHY -> MediumGreen
        AnimalStatus.MONITORED -> Warn
        AnimalStatus.QUARANTINE -> Danger
        AnimalStatus.DECEASED -> TextMute
    }
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSoft),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PaleGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Eco, null, tint = MediumGreen, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "${animal.id} · Lote ${animal.lot}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMute
                        )
                        Text(
                            text = animal.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    StatusPill(animal.status.label, statusColor)
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (animal.gender == AnimalGender.MALE) "Macho" else "Hembra",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMute
                    )
                    Text(
                        text = "${animal.weightKg} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMute
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward, null,
                tint = TextMute, modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun StatusPill(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.13f),
        shape = CircleShape
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}
