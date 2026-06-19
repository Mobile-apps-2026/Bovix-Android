package pe.edu.upc.bovix.cattle.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.bovix.cattle.domain.model.Animal
import pe.edu.upc.bovix.cattle.domain.model.AnimalGender
import pe.edu.upc.bovix.cattle.domain.model.AnimalStatus
import pe.edu.upc.bovix.core.ui.LocalSnackbarHostState
import pe.edu.upc.bovix.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CattleScreen(viewModel: CattleViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    var showAddMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BgPrimary)) {

        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(ForestGreen)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Text("Mi Ganado", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Box {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MintGreen)
                        .clickable { showAddMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showAddMenu, onDismissRequest = { showAddMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Agregar animal") },
                        leadingIcon = { Icon(Icons.Default.Eco, null) },
                        onClick = { showAddMenu = false; viewModel.showAddAnimalDialog() }
                    )
                    DropdownMenuItem(
                        text = { Text("Agregar lote") },
                        leadingIcon = { Icon(Icons.Default.Folder, null) },
                        onClick = { showAddMenu = false; viewModel.showAddLotDialog() }
                    )
                }
            }
        }

        // Buscador
        Surface(color = CardWhite, modifier = Modifier.fillMaxWidth()) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }

        // Chips de filtro por lote, con × para eliminar cada uno
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(state.availableLots) { lotLabel ->
                val selected = state.selectedLot == lotLabel
                val lotLetter = lotLabel.removePrefix("Lote ").trim()
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.onLotSelected(lotLabel) },
                    label = {
                        Text(
                            lotLabel,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    trailingIcon = if (lotLabel != "Todos") ({
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Eliminar Lote $lotLetter",
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { viewModel.requestDeleteLot(lotLetter) },
                            tint = if (selected) Color.White else TextMute
                        )
                    }) else null,
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

        // Lista de animales
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
                    AnimalCard(
                        animal = animal,
                        onEdit = { viewModel.showEditAnimalDialog(animal) },
                        onDelete = { viewModel.requestDeleteAnimal(animal) }
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    // Diálogo: agregar animal
    if (state.showAddAnimalDialog) {
        AnimalFormDialog(
            title = "Agregar animal",
            initial = null,
            availableLots = state.lots,
            onConfirm = { name, lot, status, gender, weight ->
                viewModel.addAnimal(name, lot, status, gender, weight)
            },
            onDismiss = viewModel::hideAddAnimalDialog
        )
    }

    // Diálogo: editar animal
    state.editingAnimal?.let { animal ->
        AnimalFormDialog(
            title = "Editar animal",
            initial = animal,
            availableLots = state.lots,
            onConfirm = { name, lot, status, gender, weight ->
                viewModel.updateAnimal(animal.copy(name = name, lot = lot, status = status, gender = gender, weightKg = weight))
            },
            onDismiss = viewModel::hideEditAnimalDialog
        )
    }

    // Diálogo: agregar lote
    if (state.showAddLotDialog) {
        AddLotDialog(
            existingLots = state.lots,
            onConfirm = viewModel::addLot,
            onDismiss = viewModel::hideAddLotDialog
        )
    }

    // Confirmar eliminación de animal
    state.deletingAnimal?.let { animal ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDeleteAnimal,
            title = { Text("Eliminar animal") },
            text = { Text("¿Eliminar a ${animal.name}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteAnimal) {
                    Text("Eliminar", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDeleteAnimal) { Text("Cancelar") }
            }
        )
    }

    // Confirmar eliminación de lote (también elimina sus animales)
    state.deletingLot?.let { lot ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDeleteLot,
            title = { Text("Eliminar Lote $lot") },
            text = { Text("Se eliminarán el lote y todos los animales que pertenecen a él. ¿Continuar?") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteLot) {
                    Text("Eliminar", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDeleteLot) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimalFormDialog(
    title: String,
    initial: Animal?,
    availableLots: List<String>,
    onConfirm: (name: String, lot: String, status: AnimalStatus, gender: AnimalGender, weightKg: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var lot by remember { mutableStateOf(initial?.lot ?: availableLots.firstOrNull() ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: AnimalStatus.HEALTHY) }
    var gender by remember { mutableStateOf(initial?.gender ?: AnimalGender.MALE) }
    var weightStr by remember { mutableStateOf(initial?.weightKg?.toString() ?: "") }
    var lotExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    val canSave = name.isNotBlank() && lot.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de lote: dropdown si hay lotes, texto libre si no
                if (availableLots.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = lotExpanded,
                        onExpandedChange = { lotExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (lot.isNotEmpty()) "Lote $lot" else "Seleccionar lote",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Lote") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(lotExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = lotExpanded,
                            onDismissRequest = { lotExpanded = false }
                        ) {
                            availableLots.forEach { l ->
                                DropdownMenuItem(
                                    text = { Text("Lote $l") },
                                    onClick = { lot = l; lotExpanded = false }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = lot,
                        onValueChange = { lot = it.trim().uppercase() },
                        label = { Text("Lote") },
                        placeholder = { Text("ej. A", color = TextMute) },
                        supportingText = { Text("Aún no hay lotes. Escribe uno nuevo.", color = TextMute) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Selector de estado
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = status.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        AnimalStatus.entries.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.label) },
                                onClick = { status = s; statusExpanded = false }
                            )
                        }
                    }
                }

                // Toggle de género
                Text("Género", style = MaterialTheme.typography.bodySmall, color = TextMute)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(AnimalGender.MALE to "Macho", AnimalGender.FEMALE to "Hembra").forEach { (g, label) ->
                        val sel = gender == g
                        OutlinedButton(
                            onClick = { gender = g },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (sel) ForestGreen else Color.Transparent,
                                contentColor = if (sel) Color.White else ForestGreen
                            ),
                            border = BorderStroke(1.dp, ForestGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Peso
                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Peso (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (canSave) onConfirm(name.trim(), lot, status, gender, weightStr.toIntOrNull() ?: 0) }, enabled = canSave) {
                Text("Guardar", color = if (canSave) ForestGreen else TextMute)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun AddLotDialog(
    existingLots: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    val isDuplicate = name.trim().uppercase() in existingLots
    val canAdd = name.isNotBlank() && !isDuplicate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar lote", style = MaterialTheme.typography.titleMedium) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del lote") },
                placeholder = { Text("ej. D", color = TextMute) },
                singleLine = true,
                isError = isDuplicate,
                supportingText = if (isDuplicate) ({
                    Text("Ya existe un lote con ese nombre", color = Danger)
                }) else null,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = canAdd) {
                Text("Agregar", color = if (canAdd) ForestGreen else TextMute)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun AnimalCard(animal: Animal, onEdit: () -> Unit, onDelete: () -> Unit) {
    val statusColor = when (animal.status) {
        AnimalStatus.HEALTHY -> MediumGreen
        AnimalStatus.MONITORED -> Warn
        AnimalStatus.QUARANTINE -> Danger
        AnimalStatus.DECEASED -> TextMute
    }
    var menuExpanded by remember { mutableStateOf(false) }

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
            // Ícono del animal
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
            Spacer(Modifier.width(4.dp))
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward, null,
                        tint = TextMute, modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = { menuExpanded = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Danger) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Danger) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
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
