package pe.edu.upc.bovix.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import pe.edu.upc.bovix.core.ui.CenteredError
import pe.edu.upc.bovix.core.ui.CenteredLoader
import pe.edu.upc.bovix.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToGanado: () -> Unit,
    onNavigateToFeed: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) {
            viewModel.consumeLogout()
            onLogout()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgPrimary)) {
        when {
            state.isLoading && state.data == null -> CenteredLoader()
            state.errorMessage != null && state.data == null ->
                CenteredError(state.errorMessage!!) { viewModel.load() }
            state.data != null -> HomeContent(
                userName = state.data!!.userName,
                stats = state.data!!.stats,
                alert = state.data!!.alert,
                activities = state.data!!.activities,
                onAvatarClick = viewModel::showProfile,
                onAlertClick = onNavigateToHealth,
                onActivityClick = { type ->
                    when (type) {
                        HomeActivityType.REGISTRATION -> onNavigateToGanado()
                        HomeActivityType.FEED_UPDATE -> onNavigateToFeed()
                        HomeActivityType.VET_VISIT -> onNavigateToHealth()
                    }
                }
            )
        }
    }

    if (state.showProfileSheet) {
        ProfileBottomSheet(
            userName = state.data?.userName ?: "",
            userEmail = state.userEmail,
            onLogout = viewModel::logout,
            onDismiss = viewModel::hideProfile
        )
    }
}

@Composable
private fun HomeContent(
    userName: String,
    stats: HomeStats,
    alert: HomeAlert?,
    activities: List<HomeActivity>,
    onAvatarClick: () -> Unit,
    onAlertClick: () -> Unit,
    onActivityClick: (HomeActivityType) -> Unit
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
                    Text(text = "Buenos días", color = MintGreen, style = MaterialTheme.typography.bodySmall)
                    Text(text = userName, color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
                // Avatar — abre el perfil
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MintGreen)
                        .clickable { onAvatarClick() },
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
        Column(
            modifier = Modifier
                .offset(y = (-20).dp)
                .padding(horizontal = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Total animales", stats.totalAnimals.toString(), Icons.Default.Favorite, MediumGreen, Modifier.weight(1f))
                StatCard("Lotes activos", stats.activeLots.toString(), Icons.Default.Folder, Amber, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Citas hoy", stats.appointmentsToday.toString(), Icons.Default.CalendarToday, Sky, Modifier.weight(1f))
                StatCard("Alertas", stats.alerts.toString(), Icons.Default.Notifications, Danger, Modifier.weight(1f))
            }

            // Alerta activa (si la hay)
            alert?.let {
                Spacer(Modifier.height(12.dp))
                AlertCard(it, onClick = onAlertClick)
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
                        ActivityRow(item, onClick = { onActivityClick(item.type) })
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
private fun StatCard(label: String, value: String, icon: ImageVector, iconTint: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(text = value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(text = label, color = TextMute, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AlertCard(alert: HomeAlert, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DangerBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Danger.copy(alpha = 0.3f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Notifications, null, tint = Danger, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(alert.title, color = Danger, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(alert.description, color = Color(0xFF7B241C), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Danger, modifier = Modifier.size(18.dp).align(Alignment.CenterVertically))
        }
    }
}

@Composable
private fun ActivityRow(activity: HomeActivity, onClick: () -> Unit) {
    val (icon, tint) = when (activity.type) {
        HomeActivityType.REGISTRATION -> Icons.Default.Add to MintGreen
        HomeActivityType.FEED_UPDATE -> Icons.Default.Star to Amber
        HomeActivityType.VET_VISIT -> Icons.Default.Favorite to Sky
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
        Icon(Icons.Default.ChevronRight, null, tint = TextMute, modifier = Modifier.size(16.dp))
    }
}

// ─── Perfil ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileBottomSheet(
    userName: String,
    userEmail: String,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Avatar grande
            Box(
                modifier = Modifier
                    .size(72.dp)
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
                    fontSize = 28.sp
                )
            }

            Spacer(Modifier.height(14.dp))
            Text(userName, style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            if (userEmail.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = TextMute)
            }

            Spacer(Modifier.height(28.dp))
            HorizontalDivider(color = BorderSoft)
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Danger, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

