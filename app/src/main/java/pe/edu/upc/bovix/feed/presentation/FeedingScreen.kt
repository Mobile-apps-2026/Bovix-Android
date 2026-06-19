package pe.edu.upc.bovix.feed.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.bovix.core.ui.LocalSnackbarHostState
import pe.edu.upc.bovix.feed.domain.model.ComponentColor
import pe.edu.upc.bovix.feed.domain.model.FeedingComponent
import pe.edu.upc.bovix.feed.domain.model.FeedingPlan
import pe.edu.upc.bovix.ui.theme.*

@Composable
fun FeedingScreen(viewModel: FeedingViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgPrimary)) {
        when {
            state.isLoading && state.plans.isEmpty() -> CenteredLoader()
            state.errorMessage != null && state.plans.isEmpty() ->
                CenteredError(state.errorMessage!!) { viewModel.load() }
            else -> FeedingContent(
                plans = state.plans,
                selectedLot = state.selectedLot ?: state.plans.firstOrNull()?.lot ?: "",
                onLotSelected = viewModel::onLotSelected,
                onEditPlan = viewModel::startEditPlan,
                onDeletePlan = viewModel::requestDeletePlan,
                onCreatePlan = viewModel::showCreatePlanDialog
            )
        }
    }

    // Diálogo: editar plan
    state.editingPlan?.let { plan ->
        PlanFormDialog(
            title = "Editar plan · Lote ${plan.lot}",
            initial = plan,
            lotEditable = false,
            existingLots = state.plans.map { it.lot },
            onConfirm = { lot, ration, count, comps ->
                viewModel.savePlanEdit(lot, ration, count, comps)
            },
            onDismiss = viewModel::cancelEditPlan
        )
    }

    // Diálogo: crear plan nuevo
    if (state.showCreatePlanDialog) {
        PlanFormDialog(
            title = "Nuevo plan alimentario",
            initial = null,
            lotEditable = true,
            existingLots = state.plans.map { it.lot },
            onConfirm = { lot, ration, count, comps ->
                viewModel.createPlan(lot, ration, count, comps)
            },
            onDismiss = viewModel::hideCreatePlanDialog
        )
    }

    // Confirmar eliminación de plan
    state.deletingPlan?.let { plan ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDeletePlan,
            title = { Text("Eliminar plan") },
            text = { Text("¿Eliminar el plan del Lote ${plan.lot}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeletePlan) {
                    Text("Eliminar", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDeletePlan) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FeedingContent(
    plans: List<FeedingPlan>,
    selectedLot: String,
    onLotSelected: (String) -> Unit,
    onEditPlan: (FeedingPlan) -> Unit,
    onDeletePlan: (FeedingPlan) -> Unit,
    onCreatePlan: () -> Unit
) {
    val plan = plans.firstOrNull { it.lot == selectedLot } ?: plans.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(ForestGreen)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Text("Plan Alimentario", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MintGreen)
                    .clickable { onCreatePlan() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, "Nuevo plan", tint = ForestGreen, modifier = Modifier.size(18.dp))
            }
        }

        // Tabs de lote
        Surface(color = CardWhite, modifier = Modifier.fillMaxWidth()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(plans, key = { it.lot }) { p ->
                    val selected = p.lot == selectedLot
                    Surface(
                        color = if (selected) MediumGreen else BgPrimary,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, if (selected) MediumGreen else BorderSoft),
                        onClick = { onLotSelected(p.lot) }
                    ) {
                        Text(
                            text = "Lote ${p.lot}",
                            color = if (selected) Color.White else TextMid,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        if (plan != null) {
            // Resumen de la ración diaria
            RationSummaryCard(plan)

            // Barras de composición
            Text(
                text = "Composición del plan",
                style = MaterialTheme.typography.titleMedium,
                color = TextMid,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
            )
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                plan.components.forEach { ComponentBar(it); Spacer(Modifier.height(10.dp)) }
            }

            // Acciones del plan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onEditPlan(plan) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Editar plan", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { onDeletePlan(plan) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                    border = BorderStroke(1.dp, Danger),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                }
            }
        } else {
            // No hay planes aún
            Box(
                modifier = Modifier.fillMaxWidth().padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sin planes alimentarios", color = TextMute, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Toca + para crear el primero", color = TextMute, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ─── Diálogo de formulario (crear y editar comparten la misma UI) ─────────────

@Composable
private fun PlanFormDialog(
    title: String,
    initial: FeedingPlan?,
    lotEditable: Boolean,
    existingLots: List<String>,
    onConfirm: (lot: String, dailyRationKg: Double, animalCount: Int, components: List<FeedingComponent>) -> Unit,
    onDismiss: () -> Unit
) {
    var lot by remember { mutableStateOf(initial?.lot ?: "") }
    var rationStr by remember { mutableStateOf(initial?.dailyRationKg?.let { formatKg(it) } ?: "") }
    var countStr by remember { mutableStateOf(initial?.animalCount?.toString() ?: "") }
    var components by remember {
        mutableStateOf(
            initial?.components?.map { ComponentDraft(it.id, it.name, it.percentage.toString(), it.color) }
                ?: listOf(ComponentDraft(newId(), "", "100", ComponentColor.MINT))
        )
    }

    val totalPct = components.sumOf { it.percentage.toIntOrNull() ?: 0 }
    val lotDuplicate = lotEditable && lot.trim().uppercase() in existingLots
    val canSave = lot.isNotBlank() && !lotDuplicate &&
        rationStr.toDoubleOrNull() != null && countStr.toIntOrNull() != null &&
        components.isNotEmpty() && components.all { it.name.isNotBlank() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Lote (solo editable al crear)
                    if (lotEditable) {
                        OutlinedTextField(
                            value = lot,
                            onValueChange = { lot = it.trim().uppercase() },
                            label = { Text("Nombre del lote") },
                            placeholder = { Text("ej. D", color = TextMute) },
                            singleLine = true,
                            isError = lotDuplicate,
                            supportingText = if (lotDuplicate) ({
                                Text("Ya existe un plan para este lote", color = Danger)
                            }) else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rationStr,
                            onValueChange = { rationStr = it },
                            label = { Text("Ración (kg/animal)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = countStr,
                            onValueChange = { countStr = it.filter { c -> c.isDigit() } },
                            label = { Text("Animales") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = BorderSoft)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Composición",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextMid,
                            modifier = Modifier.weight(1f)
                        )
                        val pctColor = if (totalPct == 100) MediumGreen else Danger
                        Text(
                            "$totalPct%",
                            style = MaterialTheme.typography.bodySmall,
                            color = pctColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            " / 100%",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMute
                        )
                    }

                    // Filas de componentes
                    components.forEachIndexed { index, comp ->
                        ComponentFormRow(
                            draft = comp,
                            onNameChange = { components = components.update(index) { copy(name = it) } },
                            onPercentageChange = { components = components.update(index) { copy(percentage = it) } },
                            onColorChange = { components = components.update(index) { copy(color = it) } },
                            onRemove = if (components.size > 1) ({
                                components = components.filterIndexed { i, _ -> i != index }
                            }) else null
                        )
                    }

                    // Botón agregar componente
                    OutlinedButton(
                        onClick = {
                            components = components + ComponentDraft(newId(), "", "0", nextColor(components.size))
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                        border = BorderStroke(1.dp, MintGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Agregar componente")
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (canSave) {
                                val ration = rationStr.toDouble()
                                val count = countStr.toInt()
                                val finalComponents = components.mapIndexed { i, d ->
                                    FeedingComponent(
                                        id = d.id,
                                        name = d.name.trim(),
                                        percentage = d.percentage.toIntOrNull() ?: 0,
                                        amountKg = ration * (d.percentage.toIntOrNull() ?: 0) / 100.0,
                                        color = d.color
                                    )
                                }
                                onConfirm(lot.trim().uppercase().ifEmpty { initial?.lot ?: "" }, ration, count, finalComponents)
                            }
                        },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentFormRow(
    draft: ComponentDraft,
    onNameChange: (String) -> Unit,
    onPercentageChange: (String) -> Unit,
    onColorChange: (ComponentColor) -> Unit,
    onRemove: (() -> Unit)?
) {
    Surface(
        color = BgPrimary,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BorderSoft),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = onNameChange,
                    label = { Text("Ingrediente") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                if (onRemove != null) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Close, "Quitar componente", tint = Danger)
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = draft.percentage,
                    onValueChange = { onPercentageChange(it.filter { c -> c.isDigit() }.take(3)) },
                    label = { Text("%") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(90.dp)
                )
                Text("Color:", style = MaterialTheme.typography.bodySmall, color = TextMute)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ComponentColor.entries.forEach { c ->
                        val tint = c.toColor()
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(tint)
                                .then(
                                    if (draft.color == c)
                                        Modifier.border(2.dp, TextPrimary, CircleShape)
                                    else Modifier
                                )
                                .clickable { onColorChange(c) }
                        )
                    }
                }
            }
        }
    }
}

// ─── Componentes de la vista principal ────────────────────────────────────────

@Composable
private fun RationSummaryCard(plan: FeedingPlan) {
    Surface(
        color = PaleGreen,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MintGreen.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Lote ${plan.lot} · ${plan.animalCount} animales",
                color = TextMid,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "${formatKg(plan.dailyRationKg)} kg",
                        color = ForestGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp
                    )
                    Text("ración diaria / animal", color = TextMid, style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatKg(plan.totalDailyKg)} kg",
                        color = MediumGreen,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("total del lote", color = TextMid, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ComponentBar(c: FeedingComponent) {
    val tint = c.color.toColor()
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(c.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(
                text = "${formatKg(c.amountKg)} kg (${c.percentage}%)",
                style = MaterialTheme.typography.bodySmall,
                color = TextMute
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BorderSoft)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(c.percentage / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint)
            )
        }
    }
}

@Composable
private fun CenteredLoader() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = ForestGreen)
    }
}

@Composable
private fun CenteredError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = TextPrimary, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)) {
            Text("Reintentar")
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private data class ComponentDraft(
    val id: String,
    val name: String,
    val percentage: String,
    val color: ComponentColor
)

private fun ComponentColor.toColor(): Color = when (this) {
    ComponentColor.MINT -> MintGreen
    ComponentColor.AMBER -> Amber
    ComponentColor.GREEN -> MediumGreen
    ComponentColor.SKY -> Sky
}

private fun formatKg(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format("%.2f", value).trimEnd('0').trimEnd('.', ',')

private fun newId() = "c-${System.currentTimeMillis()}"

private fun nextColor(index: Int): ComponentColor =
    ComponentColor.entries[index % ComponentColor.entries.size]

private fun <T> List<T>.update(index: Int, transform: T.() -> T): List<T> =
    mapIndexed { i, item -> if (i == index) item.transform() else item }
