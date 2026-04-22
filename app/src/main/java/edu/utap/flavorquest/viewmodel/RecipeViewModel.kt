package edu.utap.flavorquest.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.utap.flavorquest.data.ai.AIService
import edu.utap.flavorquest.data.local.AppDatabase
import edu.utap.flavorquest.data.model.Recipe
import edu.utap.flavorquest.data.repository.FlavorQuestRepository
import com.google.firebase.auth.FirebaseAuth
import edu.utap.flavorquest.data.model.SearchHistory
import edu.utap.flavorquest.ui.navigation.NavRoutes
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class RecipeUiState(
    val recipes: List<Recipe> = emptyList(),
    val selectedRecipe: Recipe? = null,
    val isLoading: Boolean = false,
    val chatMessage: String = "",
    val filterPriceMin: Int = 0,
    val filterPriceMax: Int = 100,
    val filterRating: Float = 0f,
    val error: String? = null,
    val savedRecipeNames: Set<String> = emptySet()
)

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.Companion.getDatabase(application)
    private val repository = FlavorQuestRepository(
        database.recipeDao(),
        database.restaurantDao(),
        database.searchHistoryDao()
    )
    private val aiService = AIService()

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    init {
        loadSavedRecipeNames()
    }

    private fun loadSavedRecipeNames() {
        viewModelScope.launch {
            repository.favoriteRecipes.collect { favorites ->
                _uiState.value = _uiState.value.copy(
                    savedRecipeNames = favorites.map { it.name }.toSet()
                )
            }
        }
    }

    fun setRecipes(recipes: List<Recipe>) {
        _uiState.value = _uiState.value.copy(recipes = recipes)
    }

    fun selectRecipe(recipe: Recipe) {
        _uiState.value = _uiState.value.copy(selectedRecipe = recipe)
        saveToHistory(recipe)
    }

    private fun saveToHistory(recipe: Recipe) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val history = SearchHistory(
                userId = user?.uid ?: "",
                title = recipe.name,
                searchType = "recipe",
                queryDetails = "${recipe.cuisine} · ${recipe.prepTime} prep · ${recipe.cookTime} cook",
                filterSummary = "${recipe.calories} kcal · ${recipe.flavorProfile}"
            )
            repository.insertHistory(history)
        }
    }

    fun clearSelectedRecipe() {
        _uiState.value = _uiState.value.copy(selectedRecipe = null)
    }

    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Prevent duplicate saves by checking if a recipe with the same name exists
                val existing = repository.getRecipeByName(recipe.name)
                if (existing != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Recipe already saved!"
                    )
                    return@launch
                }

                val recipeToSave = recipe.copy(id = 0, isFavorite = true, savedAt = System.currentTimeMillis())
                val newId = repository.insertRecipe(recipeToSave)
                Log.d("RecipeViewModel", "Recipe saved to local DB with ID: $newId")

                saveRecipeToFirebase(recipeToSave.copy(id = newId))
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Recipe saved successfully!"
                )
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to save recipe", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to save: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private suspend fun saveRecipeToFirebase(recipe: Recipe) {
        val user = Firebase.auth.currentUser ?: throw Exception("User not authenticated")

        // 1. Storage: Upload the JSON file
        val storage = Firebase.storage("gs://flavorquest-b35d8.firebasestorage.app")
        val storageRef = storage.reference
        val recipeJson = Gson().toJson(recipe)
        val timestamp = System.currentTimeMillis()
        val fileName = "${recipe.name.replace(" ", "_")}_$timestamp.json"
        val storagePath = "users/${user.uid}/recipes/$fileName"
        val recipeRef = storageRef.child(storagePath)

        recipeRef.putBytes(recipeJson.toByteArray()).await()
        val downloadUrl = recipeRef.downloadUrl.await().toString()
        Log.d("RecipeViewModel", "JSON uploaded to Storage: $storagePath")

        // 2. Firestore: Store the metadata
        val firestore = Firebase.firestore
        val docRef = firestore.collection("users")
            .document(user.uid)
            .collection("recipes")
            .document()

        val recipeMetadata = mapOf(
            "id" to docRef.id,
            "name" to recipe.name,
            "storagePath" to storagePath,
            "downloadUrl" to downloadUrl,
            "timestamp" to timestamp,
            "prepTime" to recipe.prepTime,
            "cookTime" to recipe.cookTime,
            "calories" to recipe.calories,
            "cuisine" to recipe.cuisine,
            "flavorProfile" to recipe.flavorProfile
        )

        docRef.set(recipeMetadata).await()
        Log.d("RecipeViewModel", "Metadata saved to Firestore: ${docRef.id}")
    }

    fun updateChatMessage(message: String) {
        _uiState.value = _uiState.value.copy(chatMessage = message)
    }

    fun sendChatMessage() {
        val message = _uiState.value.chatMessage
        if (message.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, chatMessage = "")
            try {
                val refinedRecipes = aiService.getRecipeSuggestions(
                    "Refine these recipe suggestions: $message"
                )
                _uiState.value = _uiState.value.copy(
                    recipes = refinedRecipes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to refine results"
                )
            }
        }
    }
}