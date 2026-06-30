package com.example.proyectofinalmoviles1.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectofinalmoviles1.data.TokenProvider
import com.example.proyectofinalmoviles1.ui.screens.groups.GroupDetailScreen
import com.example.proyectofinalmoviles1.ui.screens.groups.GroupsScreen
import com.example.proyectofinalmoviles1.ui.screens.home.HomeScreen
import com.example.proyectofinalmoviles1.ui.screens.login.LoginScreen
import com.example.proyectofinalmoviles1.ui.screens.matches.MatchDetailScreen
import com.example.proyectofinalmoviles1.ui.screens.matches.MatchesScreen
import com.example.proyectofinalmoviles1.ui.screens.profile.ProfileScreen
import com.example.proyectofinalmoviles1.ui.screens.stadiums.StadiumDetailScreen
import com.example.proyectofinalmoviles1.ui.screens.stadiums.StadiumMapScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Inicio", Icons.Default.Home)
    data object Groups : Screen("groups", "Grupos", Icons.Default.Group)
    data object Matches : Screen("matches", "Partidos", Icons.Default.SportsSoccer)
    data object Map : Screen("stadium_map", "Sedes", Icons.Default.Place)
    data object Profile : Screen("profile", "Perfil", Icons.Default.Person)
}

private val bottomNavScreens = listOf(Screen.Home, Screen.Groups, Screen.Matches, Screen.Map, Screen.Profile)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val startDest = if (TokenProvider.token != null) Screen.Home.route else "login"

    NavHost(navController = navController, startDestination = startDest) {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            MainScaffold(navController, Screen.Home) {
                HomeScreen(
                    onNavigateToGroups = { navController.navigate(Screen.Groups.route) },
                    onNavigateToMatches = { navController.navigate(Screen.Matches.route) },
                    onNavigateToMatch = { id -> navController.navigate("matches/$id") },
                    onNavigateToGroup = { id -> navController.navigate("groups/$id") }
                )
            }
        }

        composable(Screen.Groups.route) {
            MainScaffold(navController, Screen.Groups) {
                GroupsScreen(onNavigateToGroup = { id -> navController.navigate("groups/$id") })
            }
        }

        composable(
            "groups/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: return@composable
            GroupDetailScreen(
                groupId = groupId,
                onNavigateToMatch = { id -> navController.navigate("matches/$id") }
            )
        }

        composable(Screen.Matches.route) {
            MainScaffold(navController, Screen.Matches) {
                MatchesScreen(onNavigateToMatch = { id -> navController.navigate("matches/$id") })
            }
        }

        composable(
            "matches/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.IntType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getInt("matchId") ?: return@composable
            MatchDetailScreen(matchId = matchId, onNavigateToStadium = {})
        }

        composable(Screen.Map.route) {
            MainScaffold(navController, Screen.Map) {
                StadiumMapScreen(onNavigateToStadium = { id -> navController.navigate("stadiums/$id") })
            }
        }

        composable(
            "stadiums/{stadiumId}",
            arguments = listOf(navArgument("stadiumId") { type = NavType.IntType })
        ) { backStackEntry ->
            val stadiumId = backStackEntry.arguments?.getInt("stadiumId") ?: return@composable
            StadiumDetailScreen(stadiumId = stadiumId, onNavigateToMatch = { id -> navController.navigate("matches/$id") })
        }

        composable(Screen.Profile.route) {
            MainScaffold(navController, Screen.Profile) {
                ProfileScreen(onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
        }
    }
}

@Composable
private fun MainScaffold(
    navController: androidx.navigation.NavController,
    currentScreen: Screen,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            content()
        }
    }
}
