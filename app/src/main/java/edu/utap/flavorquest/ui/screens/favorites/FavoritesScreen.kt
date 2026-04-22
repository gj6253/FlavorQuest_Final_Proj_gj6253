package edu.utap.flavorquest.ui.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.utap.flavorquest.data.model.Recipe
import edu.utap.flavorquest.data.model.Restaurant
import edu.utap.flavorquest.ui.components.RecipeCard
import edu.utap.flavorquest.ui.components.RestaurantCard
import edu.utap.flavorquest.ui.components.TabSelector
import edu.utap.flavorquest.viewmodel.FavoritesUiState

@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onTabSelected: (Int) -> Unit,
    onViewRecipe: (Recipe) -> Unit,
    onRemoveRecipe: (Recipe) -> Unit,
    onViewRestaurantDetails: (Restaurant) -> Unit,
    onRemoveRestaurant: (Restaurant) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Tabs
        TabSelector(
            tabs = listOf("Saved Recipes", "Saved Restaurants"),
            selectedIndex = uiState.selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Loading indicator
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Content
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (uiState.selectedTab) {
                0 -> {
                    // Saved Recipes
                    if (uiState.savedRecipes.isEmpty()) {
                        item {
                            EmptyStateMessage("No saved recipes yet.\nSave recipes from your search results!")
                        }
                    } else {
                        item {
                            Text(
                                text = "Saved Recipes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(uiState.savedRecipes) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onViewRecipe = { onViewRecipe(recipe) },
                                onSaveRecipe = null,
                                onRemoveRecipe = { onRemoveRecipe(recipe) }
                            )
                        }
                    }
                }

                1 -> {
                    // Saved Restaurants
                    if (uiState.savedRestaurants.isEmpty()) {
                        item {
                            EmptyStateMessage("No saved restaurants yet.\nSave restaurants from your search results!")
                        }
                    } else {
                        item {
                            Text(
                                text = "Saved Restaurants",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(uiState.savedRestaurants) { restaurant ->
                            RestaurantCard(
                                restaurant = restaurant,
                                onViewDetails = { onViewRestaurantDetails(restaurant) },
                                onRemove = { onRemoveRestaurant(restaurant) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}