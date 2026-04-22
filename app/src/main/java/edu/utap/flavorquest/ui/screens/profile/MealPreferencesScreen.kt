package edu.utap.flavorquest.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.utap.flavorquest.ui.components.ChipSelector
import edu.utap.flavorquest.ui.theme.Teal700
import edu.utap.flavorquest.ui.theme.Teal800

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPreferencesScreen(
    onBack: () -> Unit
) {
    var dietaryGoal by remember { mutableStateOf("Weight Loss") }
    var spiceLevel by remember { mutableFloatStateOf(2f) }
    var selectedDiets by remember { mutableStateOf(setOf<String>()) }
    var selectedCuisines by remember { mutableStateOf(setOf<String>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Meal Preferences") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Teal800,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Personalize Your Experience",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Teal700
            )
            Text(
                text = "These preferences help us tailor your recipe and restaurant suggestions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Dietary Goal
            Text(
                text = "Main Goal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            listOf("Weight Loss", "Muscle Gain", "Healthy Living", "Budget Friendly").forEach { goal ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = dietaryGoal == goal,
                        onClick = { dietaryGoal = goal }
                    )
                    Text(text = goal, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Diet Types
            ChipSelector(
                label = "Dietary Restrictions:",
                options = listOf("Vegan", "Vegetarian", "Keto", "Paleo", "Gluten-Free", "Dairy-Free", "Halal", "Kosher"),
                selectedOptions = selectedDiets.toList(),
                onOptionToggled = { diet ->
                    selectedDiets = if (diet in selectedDiets) {
                        selectedDiets - diet
                    } else {
                        selectedDiets + diet
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Spice Level
            Text(
                text = "Preferred Spice Level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = spiceLevel,
                onValueChange = { spiceLevel = it },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = Teal700,
                    activeTrackColor = Teal700
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mild", style = MaterialTheme.typography.labelSmall)
                Text("Medium", style = MaterialTheme.typography.labelSmall)
                Text("Extra Hot", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Favorite Cuisines
            ChipSelector(
                label = "Favorite Cuisines:",
                options = listOf("Italian", "Japanese", "Mexican", "Indian", "Thai", "French", "Greek", "Chinese"),
                selectedOptions = selectedCuisines.toList(),
                onOptionToggled = { cuisine ->
                    selectedCuisines = if (cuisine in selectedCuisines) {
                        selectedCuisines - cuisine
                    } else {
                        selectedCuisines + cuisine
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal700)
            ) {
                Text("Save Preferences", style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
