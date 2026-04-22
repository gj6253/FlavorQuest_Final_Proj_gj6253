package edu.utap.flavorquest.ui.screens.recipe

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.utap.flavorquest.data.model.Recipe
import edu.utap.flavorquest.ui.components.RecipeCard
import edu.utap.flavorquest.ui.theme.Teal800
import edu.utap.flavorquest.viewmodel.RecipeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSuggestionsScreen(
    uiState: RecipeUiState,
    onBack: () -> Unit,
    onViewRecipe: (Recipe) -> Unit,
    onSaveRecipe: (Recipe) -> Unit,
    onErrorDismissed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Show Snackbar when error/success message is set
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            onErrorDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Flavor Quest",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Recipe Suggestions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Teal800
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.recipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onViewRecipe = {
                            Log.d("RecipeSuggestions", "View Recipe clicked: ${recipe.name}")
                            onViewRecipe(recipe)
                        },
                        onSaveRecipe = { onSaveRecipe(recipe) }
                    )
                }
            }
        }
    }
}