package pe.edu.upc.bovix.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pe.edu.upc.bovix.auth.presentation.login.LoginScreen
import pe.edu.upc.bovix.cattle.presentation.CattleScreen
import pe.edu.upc.bovix.health.presentation.HealthScreen
import pe.edu.upc.bovix.feed.presentation.FeedingScreen
import pe.edu.upc.bovix.home.presentation.HomeScreen
import pe.edu.upc.bovix.ui.theme.BorderSoft
import pe.edu.upc.bovix.ui.theme.CardWhite
import pe.edu.upc.bovix.ui.theme.ForestGreen
import pe.edu.upc.bovix.ui.theme.TextMute

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
}

/**
 * Pestañas de la bottom bar — solo activas dentro del shell "main".
 */
enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("tab_home", "Inicio", Icons.Default.Home),
    CATTLE("tab_cattle", "Ganado", Icons.Default.Eco),
    HEALTH("tab_health", "Salud", Icons.Default.Favorite),
    FEED("tab_feed", "Alimento", Icons.Default.Restaurant)
}

/* =====================================================================
 * Root NavGraph: gestiona Login -> Shell principal
 * ===================================================================== */
@Composable
fun BovixNavGraph() {
    val rootNav = rememberNavController()
    NavHost(navController = rootNav, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    rootNav.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.MAIN) {
            MainShell()
        }
    }
}

/* =====================================================================
 * Shell principal con Bottom Navigation Bar + NavHost anidado
 * ===================================================================== */
@Composable
private fun MainShell() {
    val tabNav = rememberNavController()
    Scaffold(
        bottomBar = { BovixBottomBar(tabNav) },
        containerColor = CardWhite
    ) { padding ->
        NavHost(
            navController = tabNav,
            startDestination = BottomTab.HOME.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomTab.HOME.route) { HomeScreen() }
            composable(BottomTab.CATTLE.route) { CattleScreen() }
            composable(BottomTab.HEALTH.route) { HealthScreen() }
            composable(BottomTab.FEED.route) { FeedingScreen() }
        }
    }
}

@Composable
private fun BovixBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(
        containerColor = CardWhite,
        tonalElevation = 0.dp,
        contentColor = ForestGreen
    ) {
        BottomTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route ||
                backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ForestGreen,
                    selectedTextColor = ForestGreen,
                    unselectedIconColor = TextMute,
                    unselectedTextColor = TextMute,
                    indicatorColor = BorderSoft
                )
            )
        }
    }
}
