package edu.utap.flavorquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.utap.flavorquest.ui.navigation.FlavorQuestBottomBar
import edu.utap.flavorquest.ui.navigation.FlavorQuestNavGraph
import edu.utap.flavorquest.ui.navigation.NavRoutes
import edu.utap.flavorquest.ui.navigation.bottomNavItems
import edu.utap.flavorquest.ui.theme.FlavorQuestTheme
import edu.utap.flavorquest.viewmodel.AuthViewModel
import edu.utap.flavorquest.viewmodel.HomeViewModel
import kotlin.collections.contains

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlavorQuestTheme {
                FlavorQuestMainScreen()
            }
        }
    }
}

@Composable
fun FlavorQuestMainScreen() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Screens that show the bottom nav bar
    val showBottomBar = currentRoute in listOf(
        NavRoutes.HOME,
        NavRoutes.FAVORITES,
        NavRoutes.HISTORY,
        NavRoutes.PROFILE,
        NavRoutes.RECIPE_SUGGESTIONS,
        NavRoutes.RECIPE_DETAIL,
        NavRoutes.RESTAURANT_RESULTS,
        NavRoutes.RESTAURANT_DETAIL
    )

    // Handle auth state changes
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn && currentRoute in listOf(NavRoutes.LOGIN, NavRoutes.REGISTER)) {
            navController.navigate(NavRoutes.HOME) {
                popUpTo(0) { inclusive = true }
            }
        } else if (!authState.isLoggedIn && currentRoute in listOf(NavRoutes.LOGIN, NavRoutes.REGISTER)) {
            navController.navigate(NavRoutes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FlavorQuestBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route == NavRoutes.HOME && currentRoute != NavRoutes.HOME) {
                            navController.navigate(NavRoutes.HOME) {
                                popUpTo(NavRoutes.HOME) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(route) {
                                popUpTo(NavRoutes.HOME) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        FlavorQuestNavGraph(
            navController = navController,
            authViewModel = authViewModel,
            isLoggedIn = authState.isLoggedIn,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
