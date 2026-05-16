package pe.edu.upc.bovix.health.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
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
import pe.edu.upc.bovix.health.domain.model.AlertSeverity
import pe.edu.upc.bovix.health.domain.model.ClinicalEntry
import pe.edu.upc.bovix.health.domain.model.PendingVaccination
import pe.edu.upc.bovix.health.domain.model.VetAppointment
import pe.edu.upc.bovix.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HealthScreen(viewModel: HealthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(BgPrimary)) {
        when {
            state.isLoading && state.data == null -> CenteredLoader()
            state.errorMessage != null && state.data == null ->
                CenteredError(state.errorMessage!!) { viewModel.load() }
            state.data != null -> HealthContent(
                appointment = state.data!!.nextAppointment,
                pending = state.data!!.pendingVaccinations,
                history = state.data!!.clinicalHistory
            )
        }
    }
}

@Composable
private fun HealthContent(
    appointment: VetAppointment?,
    pending: List<PendingVaccination>,
    history: List<ClinicalEntry>
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
                    .background(MintGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = ForestGreen, modifier = Modifier.size(18.dp))
            }
        }

        // Próxima cita
        appointment?.let { NextAppointmentCard(it) }

        // Vacunaciones pendientes
        if (pending.isNotEmpty()) {
            SectionHeader("Vacunación pendiente")
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                pending.forEach { PendingVaccineRow(it); Spacer(Modifier.height(8.dp)) }
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
private fun NextAppointmentCard(appt: VetAppointment) {
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
                Text(
                    v.vaccineName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    v.lotLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMute
                )
            }
            Text(
                v.dueLabel,
                color = fg,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
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
            tint = Sky, modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.title,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
            Text(
                entry.dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = TextMute
            )
        }
        entry.severity?.let { sev ->
            val (label, fg, bg) = severityPill(sev)
            Surface(
                color = bg,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    label,
                    color = fg,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

private fun severityPill(sev: AlertSeverity): Triple<String, Color, Color> = when (sev) {
    AlertSeverity.HIGH -> Triple("Alta", Danger, DangerBg)
    AlertSeverity.MEDIUM -> Triple("Media", Warn, WarnBg)
    AlertSeverity.LOW -> Triple("Baja", Sky, SkyBg)
}

private fun formatScheduled(date: java.time.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
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
