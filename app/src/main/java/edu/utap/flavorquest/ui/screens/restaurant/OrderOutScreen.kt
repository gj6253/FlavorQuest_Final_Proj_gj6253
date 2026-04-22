package edu.utap.flavorquest.ui.screens.restaurant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.utap.flavorquest.data.model.OrderOutProfile
import edu.utap.flavorquest.ui.theme.Teal700
import edu.utap.flavorquest.ui.theme.Teal800
import edu.utap.flavorquest.ui.components.ChipSelector
import edu.utap.flavorquest.ui.components.PriceSelector
import edu.utap.flavorquest.ui.components.RatingBar
import edu.utap.flavorquest.ui.components.TabSelector

@Composable
fun OrderOutScreen(
    profile: OrderOutProfile,
    onProfileChanged: (OrderOutProfile) -> Unit,
    onAdviseRestaurants: () -> Unit,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Teal800
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Flavor Quest",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Your AI Meal Companion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }

        // Tab selector showing Order Out selected
        TabSelector(
            tabs = listOf("Cook at Home", "Order Out"),
            selectedIndex = 1,
            onTabSelected = {},
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Restaurant Style
            ChipSelector(
                label = "Restaurant Style:",
                options = listOf("American", "Mexican", "Continental", "Mediterranean", "Chinese"),
                selectedOptions = profile.restaurantStyles,
                onOptionToggled = { style ->
                    val newStyles = if (style in profile.restaurantStyles) {
                        profile.restaurantStyles - style
                    } else {
                        profile.restaurantStyles + style
                    }
                    onProfileChanged(profile.copy(restaurantStyles = newStyles))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Restaurant Type - updated options
            ChipSelector(
                label = "Restaurant Type:",
                options = listOf("Fine Dining", "Fast Casual", "Cafe", "Buffet", "Bar", "Pub"),
                selectedOptions = profile.restaurantTypes,
                onOptionToggled = { type ->
                    val newTypes = if (type in profile.restaurantTypes) {
                        profile.restaurantTypes - type
                    } else {
                        profile.restaurantTypes + type
                    }
                    onProfileChanged(profile.copy(restaurantTypes = newTypes))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Price selector
            PriceSelector(
                selectedLevel = profile.priceLevel,
                onLevelSelected = { onProfileChanged(profile.copy(priceLevel = it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rating filter
            Text(
                text = "Filter by Minimum Rating:",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            RatingBar(
                rating = profile.minimumRating,
                starSize = 32.dp,
                onRatingChanged = { onProfileChanged(profile.copy(minimumRating = it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Additional Details free text box
            Text(
                text = "Tell us more:",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = profile.additionalDetails,
                onValueChange = { onProfileChanged(profile.copy(additionalDetails = it)) },
                placeholder = {
                    Text("e.g., I want a cozy Italian place with live music near downtown")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Suggest Restaurant button
            Button(
                onClick = onAdviseRestaurants,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal700),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Suggest Restaurant", style = MaterialTheme.typography.titleSmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}