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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import pe.edu.upc.bovix.auth.presentation.register.RegisterScreen
import pe.edu.upc.bovix.cattle.presentation.CattleScreen
import pe.edu.upc.bovix.core.ui.LocalSnackbarHostState
import pe.edu.upc.bovix.health.presentation.HealthScreen
import pe.edu.upc.bovix.feed.presentation.FeedingScreen
import pe.edu.upc.bovix.home.presentation.HomeScreen
import pe.edu.upc.bovix.ui.theme.BorderSoft
import pe.edu.upc.bovix.ui.theme.CardWhite
import pe.edu.upc.bovix.ui.theme.ForestGreen
import pe.edu.upc.bovix.ui.theme.TextMute

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
}

enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("tab_home", "Inicio", Icons.Default.Home),
    CATTLE("tab_cattle", "Ganado", Icons.Default.Eco),
    HEALTH("tab_health", "Salud", Icons.Default.Favorite),
    FEED("tab_feed", "Alimento", Icons.Default.Restaurant)
}

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
                },
                onRegisterClick = { rootNav.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    rootNav.navigate(Routes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackToLogin = { rootNav.popBackStack() }
            )
        }
        composable(Routes.MAIN) {
            MainShell(onLogout = {
                rootNav.navigate(Routes.LOGIN) {
                    popUpTo(Routes.MAIN) { inclusive = true }
                }
            })
        }
    }
}

@Composable
private fun MainShell(onLogout: () -> Unit) {
    val tabNav = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = { BovixBottomBar(tabNav) },
            containerColor = CardWhite
        ) { padding ->
            NavHost(
                navController = tabNav,
                startDestination = BottomTab.HOME.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(BottomTab.HOME.route) {
                    HomeScreen(
                        onLogout = onLogout,
                        onNavigateToHealth = { tabNav.navigateToTab(BottomTab.HEALTH) },
                        onNavigateToGanado = { tabNav.navigateToTab(BottomTab.CATTLE) },
                        onNavigateToFeed = { tabNav.navigateToTab(BottomTab.FEED) }
                    )
                }
                composable(BottomTab.CATTLE.route) { CattleScreen() }
                composable(BottomTab.HEALTH.route) { HealthScreen() }
                composable(BottomTab.FEED.route) { FeedingScreen() }
            }
        }
    }
}

private fun NavHostController.navigateToTab(tab: BottomTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
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
                onClick = { navController.navigateToTab(tab) },
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
