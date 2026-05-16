package pe.edu.upc.bovix.feed.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.bovix.feed.domain.model.ComponentColor
import pe.edu.upc.bovix.feed.domain.model.FeedingComponent
import pe.edu.upc.bovix.feed.domain.model.FeedingPlan
import pe.edu.upc.bovix.ui.theme.*

@Composable
fun FeedingScreen(viewModel: FeedingViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(BgPrimary)) {
        when {
            state.isLoading && state.plans.isEmpty() -> CenteredLoader()
            state.errorMessage != null && state.plans.isEmpty() ->
                CenteredError(state.errorMessage!!) { viewModel.load() }
            state.plans.isNotEmpty() -> FeedingContent(
                plans = state.plans,
                selectedLot = state.selectedLot ?: state.plans.first().lot,
                onLotSelected = viewModel::onLotSelected
            )
        }
    }
}

@Composable
private fun FeedingContent(
    plans: List<FeedingPlan>,
    selectedLot: String,
    onLotSelected: (String) -> Unit
) {
    val plan = plans.firstOrNull { it.lot == selectedLot } ?: plans.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ForestGreen)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Text(
                "Plan Alimentario",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }

        // Tabs de lote
        Surface(
            color = CardWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
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
                        border = BorderStroke(
                            1.dp,
                            if (selected) MediumGreen else BorderSoft
                        ),
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

        // Botón de edición
        Button(
            onClick = { /* TODO: navegar a editar plan */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .height(46.dp)
        ) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Editar plan", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))
    }
}

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
                    Text(
                        "ración diaria / animal",
                        color = TextMid,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatKg(plan.totalDailyKg)} kg",
                        color = MediumGreen,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "total del lote",
                        color = TextMid,
                        style = MaterialTheme.typography.bodySmall
                    )
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
            Text(
                c.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
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

private fun ComponentColor.toColor(): Color = when (this) {
    ComponentColor.MINT -> MintGreen
    ComponentColor.AMBER -> Amber
    ComponentColor.GREEN -> MediumGreen
    ComponentColor.SKY -> Sky
}

private fun formatKg(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format("%.2f", value).trimEnd('0').trimEnd('.', ',')

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
        Text(
            message,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) {
            Text("Reintentar")
        }
    }
}
