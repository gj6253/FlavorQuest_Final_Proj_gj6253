package edu.utap.flavorquest.ui.screens.restaurant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.utap.flavorquest.data.model.Restaurant
import edu.utap.flavorquest.ui.components.RestaurantCard
import edu.utap.flavorquest.ui.theme.Teal700
import edu.utap.flavorquest.ui.theme.Teal800
import edu.utap.flavorquest.viewmodel.RestaurantUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantResultsScreen(
    uiState: RestaurantUiState,
    onBack: () -> Unit,
    onViewDetails: (Restaurant) -> Unit,
    onAdviseRestaurants: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with back button
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Flavor Quest",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Your AI Meal Companion",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Teal800
            )
        )

        // Filter summary row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Teal800
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val firstRestaurant = uiState.restaurants.firstOrNull()
                if (firstRestaurant != null) {
                    Text(
                        text = "${firstRestaurant.cuisine} · ${firstRestaurant.type} · ${"$".repeat(firstRestaurant.priceLevel)} · Rating: ${firstRestaurant.rating}+",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    )
                }
                TextButton(onClick = { onBack() }) {
                    Text(
                        "Change",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // Restaurant list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.restaurants) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onViewDetails = { onViewDetails(restaurant) }
                )
            }
        }
    }
}