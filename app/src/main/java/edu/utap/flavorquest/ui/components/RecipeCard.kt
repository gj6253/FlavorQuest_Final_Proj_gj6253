package edu.utap.flavorquest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun RecipeCard(
    recipe: Recipe,
    onViewRecipe: () -> Unit,
    onSaveRecipe: (() -> Unit)? = null,  // nullable — null hides the button
    onRemoveRecipe: (() -> Unit)? = null, // nullable — null hides the button
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Recipe image placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
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
                    Text("\uD83C\uDF7D\uFE0F", style = MaterialTheme.typography.headlineMedium)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Prep: ${recipe.prepTime} | Cook: ${recipe.cookTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Calories: ~${recipe.calories} cal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Match percentage badge
                val matchColor = when {
                    recipe.matchPercentage >= 90 -> Green
                    recipe.matchPercentage >= 70 -> Orange
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = matchColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${recipe.matchPercentage}% Match${if (recipe.matchNote.isNotBlank()) " (${recipe.matchNote})" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = matchColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onViewRecipe,
                        colors = ButtonDefaults.buttonColors(containerColor = Teal700),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("View Recipe", style = MaterialTheme.typography.labelMedium)
                    }

                    // Only show Save button if onSaveRecipe is provided
                    if (onSaveRecipe != null) {
                        OutlinedButton(
                            onClick = onSaveRecipe,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Save", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // Only show Remove button if onRemoveRecipe is provided
                    if (onRemoveRecipe != null) {
                        OutlinedButton(
                            onClick = onRemoveRecipe,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Remove", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}