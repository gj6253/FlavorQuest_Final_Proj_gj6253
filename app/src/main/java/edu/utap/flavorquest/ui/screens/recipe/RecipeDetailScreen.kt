package edu.utap.flavorquest.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import edu.utap.flavorquest.data.model.Recipe
import edu.utap.flavorquest.ui.theme.Green
import edu.utap.flavorquest.ui.theme.Orange
import edu.utap.flavorquest.ui.theme.Teal700
import edu.utap.flavorquest.ui.theme.Teal800
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit,
    onSave: (() -> Unit)? = null,
    isSaved: Boolean = false,
    isLoading: Boolean = false
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with back button
        TopAppBar(
            title = {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            actions = {
                if (onSave != null) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 12.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = onSave) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save Recipe",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Teal800
            )
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Recipe image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (recipe.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "\uD83C\uDF7D\uFE0F",
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Match percentage badge
            val matchColor = when {
                recipe.matchPercentage >= 90 -> Green
                recipe.matchPercentage >= 70 -> Orange
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = matchColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "${recipe.matchPercentage}% Match${if (recipe.matchNote.isNotBlank()) " (${recipe.matchNote})" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = matchColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info row: Prep Time, Cook Time, Calories
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoItem(label = "Prep Time", value = recipe.prepTime)
                    InfoItem(label = "Cook Time", value = recipe.cookTime)
                    InfoItem(label = "Calories", value = "${recipe.calories} cal")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cuisine & Flavor Profile
            if (recipe.cuisine.isNotBlank() || recipe.flavorProfile.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (recipe.cuisine.isNotBlank()) {
                            Row {
                                Text(
                                    text = "Cuisine: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = recipe.cuisine,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        if (recipe.flavorProfile.isNotBlank()) {
                            if (recipe.cuisine.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Row {
                                Text(
                                    text = "Flavor: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = recipe.flavorProfile,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Ingredients section
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Teal700
            )
            Spacer(modifier = Modifier.height(8.dp))

            val ingredients = parseJsonArray(recipe.ingredients)
            ingredients.forEachIndexed { index, ingredient ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "\u2022  ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Teal700
                    )
                    Text(
                        text = ingredient,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Steps section
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Teal700
            )
            Spacer(modifier = Modifier.height(8.dp))

            val steps = parseJsonArray(recipe.steps)
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}. ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Teal700
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun parseJsonArray(jsonString: String): List<String> {
    return try {
        val jsonArray = JSONArray(jsonString)
        (0 until jsonArray.length()).map { jsonArray.getString(it) }
    } catch (e: Exception) {
        // Fallback: if it's not valid JSON, split by newline or return as single item
        if (jsonString.isBlank()) emptyList()
        else jsonString.split("\n").filter { it.isNotBlank() }
    }
}