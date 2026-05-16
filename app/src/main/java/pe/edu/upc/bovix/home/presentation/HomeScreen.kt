package pe.edu.upc.bovix.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.bovix.home.domain.model.HomeActivity
import pe.edu.upc.bovix.home.domain.model.HomeActivityType
import pe.edu.upc.bovix.home.domain.model.HomeAlert
import pe.edu.upc.bovix.home.domain.model.HomeStats
import pe.edu.upc.bovix.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(BgPrimary)) {
        when {
            state.isLoading && state.data == null -> CenteredLoader()
            state.errorMessage != null && state.data == null -> CenteredError(state.errorMessage!!) { viewModel.load() }
            state.data != null -> HomeContent(
                userName = state.data!!.userName,
                stats = state.data!!.stats,
                alert = state.data!!.alert,
                activities = state.data!!.activities
            )
        }
    }
}

@Composable
private fun HomeContent(
    userName: String,
    stats: HomeStats,
    alert: HomeAlert?,
    activities: List<HomeActivity>
) {
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
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Buenos días",
                        color = MintGreen,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = userName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MintGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .take(2).joinToString(""),
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Grid de stats, con offset negativo para solaparse con el header
        Column(modifier = Modifier
            .offset(y = (-20).dp)
            .padding(horizontal = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard("Total animales", stats.totalAnimals.toString(), Icons.Default.Favorite, MediumGreen, Modifier.weight(1f))
                StatCard("Lotes activos", stats.activeLots.toString(), Icons.Default.Folder, Amber, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard("Citas hoy", stats.appointmentsToday.toString(), Icons.Default.CalendarToday, Sky, Modifier.weight(1f))
                StatCard("Alertas", stats.alerts.toString(), Icons.Default.Notifications, Danger, Modifier.weight(1f))
            }

            // Alerta activa (si la hay)
            alert?.let {
                Spacer(Modifier.height(12.dp))
                AlertCard(it)
            }

            // Actividad reciente
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Actividad reciente",
                style = MaterialTheme.typography.titleMedium,
                color = TextMid,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    activities.forEachIndexed { index, item ->
                        ActivityRow(item)
                        if (index < activities.lastIndex) {
                            HorizontalDivider(color = BorderSoft, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(text = label, color = TextMute, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AlertCard(alert: HomeAlert) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DangerBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Danger.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Notifications, null, tint = Danger, modifier = Modifier.size(20.dp))
            Column {
                Text(alert.title, color = Danger, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(alert.description, color = Color(0xFF7B241C), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ActivityRow(activity: HomeActivity) {
    val (icon, tint) = when (activity.type) {
        HomeActivityType.REGISTRATION -> Icons.Default.Add to MintGreen
        HomeActivityType.FEED_UPDATE -> Icons.Default.Star to Amber
        HomeActivityType.VET_VISIT -> Icons.Default.Favorite to Sky
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(activity.title, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(activity.subtitle, color = TextMute, style = MaterialTheme.typography.bodySmall)
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
        Icon(Icons.Default.WarningAmber, null, tint = Danger, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(8.dp))
        Text(message, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)) {
            Text("Reintentar")
        }
    }
}
