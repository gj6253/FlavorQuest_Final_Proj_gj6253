package edu.utap.flavorquest.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import edu.utap.flavorquest.data.model.CookingProfile
import edu.utap.flavorquest.data.model.CookingTab
import edu.utap.flavorquest.data.model.MoodType
import edu.utap.flavorquest.data.model.OrderOutProfile
import edu.utap.flavorquest.ui.theme.Teal700
import edu.utap.flavorquest.ui.theme.Teal800
import edu.utap.flavorquest.viewmodel.HomeUiState
import edu.utap.flavorquest.ui.components.ChipSelector
import edu.utap.flavorquest.ui.components.MoodSelector
import edu.utap.flavorquest.ui.components.PriceSelector
import edu.utap.flavorquest.ui.components.RatingBar
import edu.utap.flavorquest.ui.components.TabSelector

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onMoodSelected: (MoodType) -> Unit,
    onTabSelected: (CookingTab) -> Unit,
    onCookingProfileChanged: (CookingProfile) -> Unit,
    onOrderOutProfileChanged: (OrderOutProfile) -> Unit,
    onGenerateSuggestions: () -> Unit
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

        Column(modifier = Modifier.padding(16.dp)) {
            // Mood Selector
            MoodSelector(
                selectedMood = uiState.selectedMood,
                onMoodSelected = onMoodSelected
            )

            Spacer(modifier = Modifier.height(20.dp))

            // The Plan section
            Text(
                text = "The Plan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cook at Home / Order Out tabs
            TabSelector(
                tabs = listOf("Cook at Home", "Order Out"),
                selectedIndex = if (uiState.selectedTab == CookingTab.COOK_AT_HOME) 0 else 1,
                onTabSelected = { index ->
                    onTabSelected(if (index == 0) CookingTab.COOK_AT_HOME else CookingTab.ORDER_OUT)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tab content
            when (uiState.selectedTab) {
                CookingTab.COOK_AT_HOME -> CookAtHomeContent(
                    profile = uiState.cookingProfile,
                    onProfileChanged = onCookingProfileChanged
                )
                CookingTab.ORDER_OUT -> OrderOutContent(
                    profile = uiState.orderOutProfile,
                    onProfileChanged = onOrderOutProfileChanged
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Generate button
            Button(
                onClick = onGenerateSuggestions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal700),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = when (uiState.selectedTab) {
                            CookingTab.COOK_AT_HOME -> "Generate Recipe"
                            CookingTab.ORDER_OUT -> "Suggest Restaurant"
                        },
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            // Error
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CookAtHomeContent(
    profile: CookingProfile,
    onProfileChanged: (CookingProfile) -> Unit
) {
    var ingredientInput by remember { mutableStateOf("") }

    Column {
        Text(
            text = "Cooking Profile",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Allergies
        OutlinedTextField(
            value = profile.allergies,
            onValueChange = { onProfileChanged(profile.copy(allergies = it)) },
            label = { Text("Any Allergies?") },
            placeholder = { Text("e.g. Peanuts, Dairy, Gluten") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Diet Type
        OutlinedTextField(
            value = profile.dietType,
            onValueChange = { onProfileChanged(profile.copy(dietType = it)) },
            label = { Text("Diet Type") },
            placeholder = { Text("e.g. Keto, Vegan, Vegetarian") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Specific Ingredients
        Text(
            text = "Specific Ingredient?",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Add ingredient input (moved above the chips)
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = ingredientInput,
                onValueChange = { ingredientInput = it },
                placeholder = { Text("Add ingredient") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (ingredientInput.isNotBlank()) {
                        onProfileChanged(profile.copy(ingredients = listOf(ingredientInput.trim()) + profile.ingredients))
                        ingredientInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add ingredient")
            }
        }

        // User-added ingredient chips (listed below input, newest at top)
        if (profile.ingredients.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                profile.ingredients.forEach { ingredient ->
                    InputChip(
                        selected = true,
                        onClick = {
                            onProfileChanged(profile.copy(ingredients = profile.ingredients - ingredient))
                        },
                        label = { Text(ingredient) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove $ingredient",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cooking Time
        Text(
            text = "Cooking Time:",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(4.dp))

        ChipSelector(
            label = "",
            options = listOf("<30min", "30min", "45min", "60+"),
            selectedOptions = listOf(
                when {
                    profile.cookingTimeMax <= 30 -> "<30min"
                    profile.cookingTimeMax <= 35 -> "30min"
                    profile.cookingTimeMax <= 50 -> "45min"
                    else -> "60+"
                }
            ),
            onOptionToggled = { time ->
                val (min, max) = when (time) {
                    "<30min" -> 0 to 30
                    "30min" -> 25 to 35
                    "45min" -> 35 to 50
                    "60+" -> 55 to 120
                    else -> 30 to 60
                }
                onProfileChanged(profile.copy(cookingTimeMin = min, cookingTimeMax = max))
            },
            singleSelection = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 15 min resume slider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "15minute resume",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Slider(
                value = profile.cookingTimeMax.toFloat(),
                onValueChange = {
                    onProfileChanged(profile.copy(cookingTimeMax = it.toInt()))
                },
                valueRange = 15f..120f,
                steps = 7,
                modifier = Modifier.weight(2f),
                colors = SliderDefaults.colors(
                    thumbColor = Teal700,
                    activeTrackColor = Teal700
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Calories
        OutlinedTextField(
            value = profile.calories,
            onValueChange = { onProfileChanged(profile.copy(calories = it)) },
            label = { Text("Calories") },
            placeholder = { Text("Enter target calorie range...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun OrderOutContent(
    profile: OrderOutProfile,
    onProfileChanged: (OrderOutProfile) -> Unit
) {
    Column {
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

        // Restaurant Type
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

        // Free Search / Additional Details
        OutlinedTextField(
            value = profile.additionalDetails,
            onValueChange = { onProfileChanged(profile.copy(additionalDetails = it)) },
            label = { Text("What are you looking for?") },
            placeholder = { Text("e.g. spicy vegetarian food in tampa") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}