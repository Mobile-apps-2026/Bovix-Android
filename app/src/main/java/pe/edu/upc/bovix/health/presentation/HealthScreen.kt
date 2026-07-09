package pe.edu.upc.bovix.health.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.bovix.core.ui.LocalSnackbarHostState
import pe.edu.upc.bovix.health.domain.model.AlertSeverity
import pe.edu.upc.bovix.health.domain.model.ClinicalEntry
import pe.edu.upc.bovix.health.domain.model.PendingVaccination
import pe.edu.upc.bovix.health.domain.model.VetAppointment
import pe.edu.upc.bovix.core.ui.CenteredError
import pe.edu.upc.bovix.core.ui.CenteredLoader
import pe.edu.upc.bovix.ui.theme.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(viewModel: HealthViewModel = hiltViewModel()) {
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
            state.isLoading && state.data == null -> CenteredLoader()
            state.errorMessage != null && state.data == null ->
                CenteredError(state.errorMessage!!) { viewModel.load() }
            else -> HealthContent(
                appointment = state.data?.nextAppointment,
                pending = state.data?.pendingVaccinations ?: emptyList(),
                history = state.data?.clinicalHistory ?: emptyList(),
                onSchedule = viewModel::showScheduleDialog,
                onCancelAppointment = viewModel::requestCancelAppointment
            )
        }
    }

    // Diálogo: agendar cita
    if (state.showScheduleDialog) {
        ScheduleAppointmentDialog(
            hasExisting = state.data?.nextAppointment != null,
            availableLots = state.availableLots,
            availableVets = state.availableVets,
            onConfirm = { vetId, vetName, lots, dateTime ->
                viewModel.scheduleAppointment(vetId, vetName, lots, dateTime)
            },
            onDismiss = viewModel::hideScheduleDialog
        )
    }

    // Diálogo: confirmar cancelación de cita
    if (state.confirmCancelAppointment) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCancelAppointment,
            title = { Text("Cancelar cita") },
            text = { Text("¿Cancelar la cita agendada? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmCancelAppointment) {
                    Text("Cancelar cita", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissCancelAppointment) { Text("Volver") }
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthContent(
    appointment: VetAppointment?,
    pending: List<PendingVaccination>,
    history: List<ClinicalEntry>,
    onSchedule: () -> Unit,
    onCancelAppointment: () -> Unit
) {
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
            Text("Salud Animal", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MintGreen)
                    .clickable { onSchedule() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = ForestGreen, modifier = Modifier.size(18.dp))
            }
        }

        // Próxima cita
        if (appointment != null) {
            NextAppointmentCard(appointment, onCancelAppointment)
        } else {
            NoAppointmentBanner(onSchedule)
        }

        // Vacunaciones pendientes
        SectionHeader("Vacunación pendiente")
        if (pending.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                pending.forEach {
                    PendingVaccineRow(it)
                    Spacer(Modifier.height(8.dp))
                }
            }
        } else {
            SpecialistInfoBanner(
                icon = Icons.Default.Vaccines,
                message = "Las vacunas son programadas y confirmadas por el especialista de salud animal"
            )
        }

        // Historial clínico
        SectionHeader("Historial clínico")
        if (history.isNotEmpty()) {
            ClinicalHistoryCard(history)
        } else {
            SpecialistInfoBanner(
                icon = Icons.Default.Description,
                message = "El historial clínico es registrado por el especialista de salud animal"
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun NextAppointmentCard(appt: VetAppointment, onCancel: () -> Unit) {
    Surface(
        color = Sky,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Próxima cita",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(2.dp))
            Text(
                appt.veterinarianName,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${formatScheduled(appt.scheduledAt)} · ${appt.lots}",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp),
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Cancelar",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Ver detalles",
                        color = Sky,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun NoAppointmentBanner(onSchedule: () -> Unit) {
    Surface(
        color = BgPrimary,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSoft),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.CalendarToday, null, tint = TextMute, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Sin cita agendada", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Text("Agenda una visita veterinaria", style = MaterialTheme.typography.bodySmall, color = TextMute)
            }
            TextButton(onClick = onSchedule) {
                Text("Agendar", color = Sky, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextMid,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun PendingVaccineRow(v: PendingVaccination) {
    val (fg, bg) = when (v.severity) {
        AlertSeverity.HIGH -> Danger to DangerBg
        AlertSeverity.MEDIUM -> Warn to WarnBg
        AlertSeverity.LOW -> Sky to SkyBg
    }
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BorderSoft),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, null, tint = fg, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(v.vaccineName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(v.lotLabel, style = MaterialTheme.typography.bodySmall, color = TextMute)
            }
            Text(v.dueLabel, color = fg, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SpecialistInfoBanner(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Surface(
        color = BgPrimary,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSoft),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(icon, null, tint = TextMute, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = TextMute)
        }
    }
}

@Composable
private fun ClinicalHistoryCard(history: List<ClinicalEntry>) {
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSoft),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            history.forEachIndexed { i, entry ->
                ClinicalHistoryRow(entry)
                if (i < history.lastIndex) {
                    HorizontalDivider(color = BorderSoft, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun ClinicalHistoryRow(entry: ClinicalEntry) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Default.Description, null,
            tint = Sky, modifier = Modifier.padding(top = 2.dp).size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.title, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            Text(entry.dateLabel, style = MaterialTheme.typography.bodySmall, color = TextMute)
        }
        entry.severity?.let { sev ->
            val (label, fg, bg) = severityPill(sev)
            Surface(color = bg, shape = RoundedCornerShape(10.dp)) {
                Text(
                    label, color = fg,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ─── Diálogo: agendar cita ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ScheduleAppointmentDialog(
    hasExisting: Boolean,
    availableLots: List<String>,
    availableVets: List<Pair<Int, String>>,
    onConfirm: (vetId: Int, vetName: String, lots: String, dateTime: LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedVet by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var vetDropdownExpanded by remember { mutableStateOf(false) }
    var selectedLots by remember { mutableStateOf(emptySet<String>()) }
    var hourStr by remember { mutableStateOf("09") }
    var minuteStr by remember { mutableStateOf("00") }
    var showDatePicker by remember { mutableStateOf(false) }

    val tomorrowMillis = System.currentTimeMillis() + 86_400_000L
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = tomorrowMillis)

    val selectedDate: LocalDate? = datePickerState.selectedDateMillis?.let {
        java.time.Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
    }
    val dateLabel = selectedDate?.format(DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale("es"))) ?: "Seleccionar fecha"

    val lotsString = selectedLots.sorted().joinToString(", ")
    val canSave = selectedVet != null && selectedLots.isNotEmpty() && selectedDate != null &&
        hourStr.toIntOrNull() in 0..23 && minuteStr.toIntOrNull() in 0..59

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendar cita", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasExisting) {
                    Surface(color = WarnBg, shape = RoundedCornerShape(8.dp)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Warn, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Esto reemplazará la cita actual",
                                style = MaterialTheme.typography.bodySmall,
                                color = Warn
                            )
                        }
                    }
                }

                // Selector de veterinario
                ExposedDropdownMenuBox(
                    expanded = vetDropdownExpanded,
                    onExpandedChange = { vetDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedVet?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Veterinario") },
                        placeholder = { Text("Seleccionar veterinario", color = TextMute) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vetDropdownExpanded) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = vetDropdownExpanded,
                        onDismissRequest = { vetDropdownExpanded = false }
                    ) {
                        if (availableVets.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay veterinarios registrados", color = TextMute) },
                                onClick = { vetDropdownExpanded = false }
                            )
                        } else {
                            availableVets.forEach { vet ->
                                DropdownMenuItem(
                                    text = { Text(vet.second) },
                                    onClick = {
                                        selectedVet = vet
                                        vetDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Selector de lotes
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Lotes", style = MaterialTheme.typography.bodySmall, color = TextMute)
                    if (availableLots.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableLots.forEach { lotName ->
                                val selected = lotName in selectedLots
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedLots = if (selected)
                                            selectedLots - lotName
                                        else
                                            selectedLots + lotName
                                    },
                                    label = { Text("Lote $lotName") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MediumGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    } else {
                        Text(
                            "No hay lotes registrados en ganado",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMute
                        )
                    }
                }

                // Selector de fecha
                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, null, tint = ForestGreen)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )

                // Hora
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hourStr,
                        onValueChange = { if (it.length <= 2) hourStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Hora") },
                        placeholder = { Text("09", color = TextMute) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Text(":", modifier = Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = minuteStr,
                        onValueChange = { if (it.length <= 2) minuteStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Min") },
                        placeholder = { Text("00", color = TextMute) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (canSave && selectedVet != null) {
                        val dt = LocalDateTime.of(
                            selectedDate!!,
                            LocalTime.of(hourStr.toInt(), minuteStr.toInt())
                        )
                        onConfirm(selectedVet!!.first, selectedVet!!.second, lotsString, dt)
                    }
                },
                enabled = canSave
            ) {
                Text("Agendar", color = if (canSave) ForestGreen else TextMute)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun severityPill(sev: AlertSeverity): Triple<String, androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> = when (sev) {
    AlertSeverity.HIGH -> Triple("Alta", Danger, DangerBg)
    AlertSeverity.MEDIUM -> Triple("Media", Warn, WarnBg)
    AlertSeverity.LOW -> Triple("Baja", Sky, SkyBg)
}

private fun formatScheduled(date: LocalDateTime): String {
    val now = LocalDateTime.now()
    val isTomorrow = date.toLocalDate() == now.toLocalDate().plusDays(1)
    val isToday = date.toLocalDate() == now.toLocalDate()
    val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale("es"))
    val dayLabel = when {
        isToday -> "Hoy"
        isTomorrow -> "Mañana"
        else -> date.format(DateTimeFormatter.ofPattern("d 'de' MMM", Locale("es")))
    }
    return "$dayLabel, ${date.format(timeFmt)}"
}

