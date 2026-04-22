package edu.utap.flavorquest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.utap.flavorquest.data.model.CookingTab
import edu.utap.flavorquest.ui.screens.auth.LoginScreen
import edu.utap.flavorquest.ui.screens.auth.RegisterScreen
import edu.utap.flavorquest.ui.screens.favorites.FavoritesScreen
import edu.utap.flavorquest.ui.screens.history.HistoryScreen
import edu.utap.flavorquest.ui.screens.home.HomeScreen
import edu.utap.flavorquest.ui.screens.profile.AboutScreen
import edu.utap.flavorquest.ui.screens.profile.HelpSupportScreen
import edu.utap.flavorquest.ui.screens.profile.MealPreferencesScreen
import edu.utap.flavorquest.ui.screens.profile.NotificationSettingsScreen
import edu.utap.flavorquest.ui.screens.profile.RateUsScreen
import edu.utap.flavorquest.ui.screens.profile.PersonalInfoScreen
import edu.utap.flavorquest.ui.screens.profile.ProfileScreen
import edu.utap.flavorquest.ui.screens.recipe.RecipeDetailScreen
import edu.utap.flavorquest.ui.screens.recipe.RecipeSuggestionsScreen
import edu.utap.flavorquest.ui.screens.restaurant.RestaurantDetailScreen
import edu.utap.flavorquest.ui.screens.restaurant.RestaurantResultsScreen
import edu.utap.flavorquest.viewmodel.AuthViewModel
import edu.utap.flavorquest.viewmodel.FavoritesViewModel
import edu.utap.flavorquest.viewmodel.HistoryViewModel
import edu.utap.flavorquest.viewmodel.HomeViewModel
import edu.utap.flavorquest.viewmodel.ProfileViewModel
import edu.utap.flavorquest.viewmodel.RecipeViewModel
import edu.utap.flavorquest.viewmodel.RestaurantViewModel

object NavRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val RECIPE_SUGGESTIONS = "recipe_suggestions"
    const val RECIPE_DETAIL = "recipe_detail"
    const val RESTAURANT_RESULTS = "restaurant_results"
    const val RESTAURANT_DETAIL = "restaurant_detail"
    const val FAVORITES = "favorites"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val PERSONAL_INFO = "personal_info"
    const val MEAL_PREFERENCES = "meal_preferences"
    const val ABOUT = "about"
    const val NOTIFICATION_SETTINGS = "notifications"
    const val HELP_SUPPORT = "help"
    const val RATE_US = "rate"
}

@Composable
fun FlavorQuestNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier
) {
    val startDestination = if (isLoggedIn) NavRoutes.HOME else NavRoutes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth screens
        composable(NavRoutes.LOGIN) {
            val authState by authViewModel.uiState.collectAsState()
            LoginScreen(
                uiState = authState,
                onSignIn = { email, password -> authViewModel.signIn(email, password) },
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.REGISTER)
                },
                onClearError = { authViewModel.clearError() }
            )
        }

        composable(NavRoutes.REGISTER) {
            val authState by authViewModel.uiState.collectAsState()
            RegisterScreen(
                uiState = authState,
                onSignUp = { name, email, password, confirm ->
                    authViewModel.signUp(name, email, password, confirm)
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onClearError = { authViewModel.clearError() }
            )
        }

        // Main screens
        composable(NavRoutes.HOME) {
            val homeViewModel: HomeViewModel = viewModel()
            val homeState by homeViewModel.uiState.collectAsState()

            // Navigate when results are ready
            LaunchedEffect(homeState.navigateToResults) {
                if (homeState.navigateToResults) {
                    homeViewModel.onNavigated()
                    when (homeState.selectedTab) {
                        CookingTab.COOK_AT_HOME -> {
                            navController.navigate(NavRoutes.RECIPE_SUGGESTIONS)
                        }
                        CookingTab.ORDER_OUT -> {
                            navController.navigate(NavRoutes.RESTAURANT_RESULTS)
                        }
                    }
                }
            }

            HomeScreen(
                uiState = homeState,
                onMoodSelected = { homeViewModel.selectMood(it) },
                onTabSelected = { homeViewModel.selectTab(it) },
                onCookingProfileChanged = { homeViewModel.updateCookingProfile(it) },
                onOrderOutProfileChanged = { homeViewModel.updateOrderOutProfile(it) },
                onGenerateSuggestions = { homeViewModel.generateSuggestions() }
            )
        }

        composable(NavRoutes.RECIPE_SUGGESTIONS) { backStackEntry ->
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavRoutes.HOME)
            }
            val homeViewModel: HomeViewModel = viewModel(homeBackStackEntry)
            val homeState by homeViewModel.uiState.collectAsState()

            val recipeViewModel: RecipeViewModel = viewModel()
            val recipeState by recipeViewModel.uiState.collectAsState()

            // Initialize with AI results from HomeViewModel
            LaunchedEffect(homeState.recipes) {
                if (homeState.recipes.isNotEmpty() && recipeState.recipes.isEmpty()) {
                    recipeViewModel.setRecipes(homeState.recipes)
                }
            }

            RecipeSuggestionsScreen(
                uiState = recipeState,
                onBack = { navController.popBackStack() },
                onViewRecipe = { recipe ->
                    recipeViewModel.selectRecipe(recipe)
                    navController.navigate(NavRoutes.RECIPE_DETAIL)
                },
                onSaveRecipe = { recipe -> recipeViewModel.saveRecipe(recipe) },
                onErrorDismissed = { recipeViewModel.clearError() }
            )
        }

        composable(NavRoutes.RECIPE_DETAIL) { backStackEntry ->
            val recipeSuggestionsBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavRoutes.RECIPE_SUGGESTIONS)
            }
            val recipeViewModel: RecipeViewModel = viewModel(recipeSuggestionsBackStackEntry)
            val recipeState by recipeViewModel.uiState.collectAsState()

            recipeState.selectedRecipe?.let { recipe ->
                RecipeDetailScreen(
                    recipe = recipe,
                    onBack = { navController.popBackStack() },
                    onSave = { recipeViewModel.saveRecipe(recipe) },
                    isSaved = recipeState.savedRecipeNames.contains(recipe.name),
                    isLoading = recipeState.isLoading
                )
            }
        }

        composable(NavRoutes.RESTAURANT_RESULTS) { backStackEntry ->
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavRoutes.HOME)
            }
            val homeViewModel: HomeViewModel = viewModel(homeBackStackEntry)
            val homeState by homeViewModel.uiState.collectAsState()

            val restaurantViewModel: RestaurantViewModel = viewModel()
            val restaurantState by restaurantViewModel.uiState.collectAsState()

            // Initialize with Places API results from HomeViewModel
            LaunchedEffect(homeState.restaurants) {
                if (homeState.restaurants.isNotEmpty() && restaurantState.restaurants.isEmpty()) {
                    restaurantViewModel.setRestaurants(homeState.restaurants)
                }
            }

            RestaurantResultsScreen(
                uiState = restaurantState,
                onBack = { navController.popBackStack() },
                onViewDetails = { restaurant ->
                    restaurantViewModel.selectRestaurant(restaurant)
                    navController.navigate(NavRoutes.RESTAURANT_DETAIL)
                },
                onAdviseRestaurants = { /* Refresh */ }
            )
        }

        composable(NavRoutes.RESTAURANT_DETAIL) { backStackEntry ->
            val restaurantResultsBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavRoutes.RESTAURANT_RESULTS)
            }
            val restaurantViewModel: RestaurantViewModel = viewModel(restaurantResultsBackStackEntry)
            val restaurantState by restaurantViewModel.uiState.collectAsState()

            restaurantState.selectedRestaurant?.let { restaurant ->
                RestaurantDetailScreen(
                    restaurant = restaurant,
                    onBack = { navController.popBackStack() },
                    onSave = { restaurantViewModel.saveRestaurant(restaurant) }
                )
            }
        }

        // FAVORITES: show detail inline to avoid getBackStackEntry issues
        composable(NavRoutes.FAVORITES) {
            val favoritesViewModel: FavoritesViewModel = viewModel()
            val favoritesState by favoritesViewModel.uiState.collectAsState()

            if (favoritesState.selectedRecipe != null) {
                // Show recipe detail inline
                RecipeDetailScreen(
                    recipe = favoritesState.selectedRecipe!!,
                    onBack = { favoritesViewModel.clearSelectedRecipe() }
                )
            } else if (favoritesState.selectedRestaurant != null) {
                // Show restaurant detail inline
                RestaurantDetailScreen(
                    restaurant = favoritesState.selectedRestaurant!!,
                    onBack = { favoritesViewModel.clearSelectedRestaurant() }
                )
            } else {
                FavoritesScreen(
                    uiState = favoritesState,
                    onTabSelected = { favoritesViewModel.selectTab(it) },
                    onViewRecipe = { recipe ->
                        favoritesViewModel.loadRecipeFromStorage(recipe)
                    },
                    onRemoveRecipe = { recipe ->
                        favoritesViewModel.removeRecipeFromFavorites(recipe)
                    },
                    onViewRestaurantDetails = { restaurant ->
                        favoritesViewModel.selectRestaurantFromFavorites(restaurant)
                    },
                    onRemoveRestaurant = { restaurant ->
                        favoritesViewModel.removeRestaurantFromFavorites(restaurant)
                    }
                )
            }
        }

        composable(NavRoutes.HISTORY) {
            val historyViewModel: HistoryViewModel = viewModel()
            val historyState by historyViewModel.uiState.collectAsState()

            HistoryScreen(
                uiState = historyState,
                onTabSelected = { historyViewModel.selectTab(it) },
                onDeleteHistory = { historyViewModel.deleteHistoryItem(it) },
                onClearAllHistory = { historyViewModel.clearAllHistory() }
            )
        }

        composable(NavRoutes.PROFILE) {
            val profileViewModel: ProfileViewModel = viewModel()
            val profile by profileViewModel.profile.collectAsState()

            ProfileScreen(
                profile = profile,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSection = { section ->
                    when (section) {
                        "personal_info" -> navController.navigate(NavRoutes.PERSONAL_INFO)
                        "meal_preferences" -> navController.navigate(NavRoutes.MEAL_PREFERENCES)
                        "about" -> navController.navigate(NavRoutes.ABOUT)
                        "notifications" -> navController.navigate(NavRoutes.NOTIFICATION_SETTINGS)
                        "help" -> navController.navigate(NavRoutes.HELP_SUPPORT)
                        "rate" -> navController.navigate(NavRoutes.RATE_US)
                    }
                }
            )
        }

        composable(NavRoutes.PERSONAL_INFO) {
            val profileViewModel: ProfileViewModel = viewModel()
            val profile by profileViewModel.profile.collectAsState()

            PersonalInfoScreen(
                profile = profile,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.MEAL_PREFERENCES) {
            MealPreferencesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ABOUT) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.HELP_SUPPORT) {
            HelpSupportScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.RATE_US) {
            RateUsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}