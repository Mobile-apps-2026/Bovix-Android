package pe.edu.upc.bovix.health.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    var showMenu by remember { mutableStateOf(false) }

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
                showMenu = showMenu,
                onMenuToggle = { showMenu = it },
                onSchedule = { showMenu = false; viewModel.showScheduleDialog() },
                onAddClinical = { showMenu = false; viewModel.showAddClinicalDialog() },
                onCancelAppointment = viewModel::requestCancelAppointment,
                onMarkVaccineDone = viewModel::markVaccineDone
            )
        }
    }

    // Diálogo: agendar cita
    if (state.showScheduleDialog) {
        ScheduleAppointmentDialog(
            hasExisting = state.data?.nextAppointment != null,
            onConfirm = { vetName, lots, dateTime ->
                viewModel.scheduleAppointment(vetName, lots, dateTime)
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

    // Diálogo: agregar entrada clínica
    if (state.showAddClinicalDialog) {
        AddClinicalEntryDialog(
            onConfirm = { title, dateLabel, severity ->
                viewModel.addClinicalEntry(title, dateLabel, severity)
            },
            onDismiss = viewModel::hideAddClinicalDialog
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthContent(
    appointment: VetAppointment?,
    pending: List<PendingVaccination>,
    history: List<ClinicalEntry>,
    showMenu: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    onSchedule: () -> Unit,
    onAddClinical: () -> Unit,
    onCancelAppointment: () -> Unit,
    onMarkVaccineDone: (String) -> Unit
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
            Box {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MintGreen)
                        .clickable { onMenuToggle(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { onMenuToggle(false) }) {
                    DropdownMenuItem(
                        text = { Text("Agendar cita") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        onClick = onSchedule
                    )
                    DropdownMenuItem(
                        text = { Text("Agregar entrada clínica") },
                        leadingIcon = { Icon(Icons.Default.Description, null) },
                        onClick = onAddClinical
                    )
                }
            }
        }

        // Próxima cita
        if (appointment != null) {
            NextAppointmentCard(appointment, onCancelAppointment)
        } else {
            NoAppointmentBanner(onSchedule)
        }

        // Vacunaciones pendientes
        if (pending.isNotEmpty()) {
            SectionHeader("Vacunación pendiente")
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                pending.forEach {
                    PendingVaccineRow(it, onDone = { onMarkVaccineDone(it.id) })
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // Historial clínico
        if (history.isNotEmpty()) {
            SectionHeader("Historial clínico")
            ClinicalHistoryCard(history)
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
private fun PendingVaccineRow(v: PendingVaccination, onDone: () -> Unit) {
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
            Spacer(Modifier.width(8.dp))
            // Marcar como aplicada
            IconButton(onClick = onDone, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.CheckCircle, "Marcar como aplicada", tint = MediumGreen, modifier = Modifier.size(20.dp))
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleAppointmentDialog(
    hasExisting: Boolean,
    onConfirm: (vetName: String, lots: String, dateTime: LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    var vetName by remember { mutableStateOf("") }
    var lots by remember { mutableStateOf("") }
    var hourStr by remember { mutableStateOf("09") }
    var minuteStr by remember { mutableStateOf("00") }
    var showDatePicker by remember { mutableStateOf(false) }

    val tomorrowMillis = System.currentTimeMillis() + 86_400_000L
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = tomorrowMillis)

    val selectedDate: LocalDate? = datePickerState.selectedDateMillis?.let {
        java.time.Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
    }
    val dateLabel = selectedDate?.format(DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale("es"))) ?: "Seleccionar fecha"

    val canSave = vetName.isNotBlank() && lots.isNotBlank() && selectedDate != null &&
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

                OutlinedTextField(
                    value = vetName,
                    onValueChange = { vetName = it },
                    label = { Text("Nombre del veterinario") },
                    placeholder = { Text("Dr. Johan Bottger", color = TextMute) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lots,
                    onValueChange = { lots = it },
                    label = { Text("Lotes") },
                    placeholder = { Text("ej. Lote A y B", color = TextMute) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                    if (canSave) {
                        val dt = LocalDateTime.of(
                            selectedDate!!,
                            LocalTime.of(hourStr.toInt(), minuteStr.toInt())
                        )
                        onConfirm(vetName, lots, dt)
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

// ─── Diálogo: agregar entrada clínica ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddClinicalEntryDialog(
    onConfirm: (title: String, dateLabel: String, severity: AlertSeverity?) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dateLabel by remember { mutableStateOf("") }
    var severityExpanded by remember { mutableStateOf(false) }
    var severity by remember { mutableStateOf<AlertSeverity?>(null) }

    val severityLabel = when (severity) {
        AlertSeverity.HIGH -> "Alta"
        AlertSeverity.MEDIUM -> "Media"
        AlertSeverity.LOW -> "Baja"
        null -> "Sin severidad"
    }
    val canSave = title.isNotBlank() && dateLabel.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva entrada clínica", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("ej. Diagnóstico: Animal #018 – Mastitis leve", color = TextMute) },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = { dateLabel = it },
                    label = { Text("Fecha") },
                    placeholder = { Text("ej. 18 jun", color = TextMute) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = severityExpanded,
                    onExpandedChange = { severityExpanded = it }
                ) {
                    OutlinedTextField(
                        value = severityLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Severidad") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(severityExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = severityExpanded,
                        onDismissRequest = { severityExpanded = false }
                    ) {
                        listOf(null, AlertSeverity.HIGH, AlertSeverity.MEDIUM, AlertSeverity.LOW).forEach { s ->
                            val label = when (s) {
                                AlertSeverity.HIGH -> "Alta"
                                AlertSeverity.MEDIUM -> "Media"
                                AlertSeverity.LOW -> "Baja"
                                null -> "Sin severidad"
                            }
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { severity = s; severityExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (canSave) onConfirm(title, dateLabel, severity) },
                enabled = canSave
            ) {
                Text("Guardar", color = if (canSave) ForestGreen else TextMute)
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
        Text(message, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)) {
            Text("Reintentar")
        }
    }
}
