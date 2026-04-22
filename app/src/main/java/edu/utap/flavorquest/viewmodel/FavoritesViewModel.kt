package edu.utap.flavorquest.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.utap.flavorquest.data.local.AppDatabase
import edu.utap.flavorquest.data.model.Recipe
import edu.utap.flavorquest.data.model.Restaurant
import edu.utap.flavorquest.data.repository.FlavorQuestRepository
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

data class FavoritesUiState(
    val selectedTab: Int = 0, // 0 = Recipes, 1 = Restaurants
    val savedRecipes: List<Recipe> = emptyList(),
    val savedRestaurants: List<Restaurant> = emptyList(),
    val selectedRecipe: Recipe? = null,
    val selectedRestaurant: Restaurant? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.Companion.getDatabase(application)
    private val repository = FlavorQuestRepository(
        database.recipeDao(),
        database.restaurantDao(),
        database.searchHistoryDao()
    )

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        val user = Firebase.auth.currentUser
        val userId = user?.uid ?: ""
        
        loadSavedRecipesFromFirestore()
        loadSavedRestaurantsFromFirestore()
        
        viewModelScope.launch {
            repository.favoriteRecipes.collect { recipes ->
                Log.d("FavoritesViewModel", "Observed ${recipes.size} favorite recipes from local DB")
                _uiState.value = _uiState.value.copy(savedRecipes = recipes)
            }
        }
        viewModelScope.launch {
            repository.favoriteRestaurants(userId).collect { restaurants ->
                _uiState.value = _uiState.value.copy(savedRestaurants = restaurants)
            }
        }
    }

    private fun loadSavedRestaurantsFromFirestore() {
        viewModelScope.launch {
            try {
                val user = Firebase.auth.currentUser
                if (user == null) return@launch

                val firestore = Firebase.firestore
                val snapshot = firestore.collection("users").document(user.uid)
                    .collection("restaurants")
                    .get()
                    .await()

                // Clear existing local favorites for this user to ensure sync
                repository.deleteAllFavoriteRestaurants(user.uid)

                snapshot.documents.forEach { doc ->
                    try {
                        val restaurant = Restaurant(
                            userId = user.uid,
                            name = doc.getString("name") ?: "",
                            rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                            distance = doc.getString("distance") ?: "",
                            priceLevel = (doc.getLong("priceLevel") ?: 2).toInt(),
                            cuisine = doc.getString("cuisine") ?: "",
                            type = doc.getString("type") ?: "",
                            amenities = doc.getString("amenities") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            address = doc.getString("address") ?: "",
                            phone = doc.getString("phone") ?: "",
                            websiteUrl = doc.getString("websiteUrl") ?: "",
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            placeId = doc.id,
                            liveMusic = doc.getBoolean("liveMusic") ?: false,
                            craftBeer = doc.getBoolean("craftBeer") ?: false,
                            outdoorPatio = doc.getBoolean("outdoorPatio") ?: false,
                            isFavorite = true,
                            savedAt = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                        repository.insertRestaurant(restaurant)
                    } catch (e: Exception) {
                        Log.e("FavoritesViewModel", "Error syncing restaurant doc: ${doc.id}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Failed to load restaurants from Firestore", e)
            }
        }
    }

    private fun loadSavedRecipesFromFirestore() {
        viewModelScope.launch {
            try {
                val user = Firebase.auth.currentUser
                if (user == null) {
                    Log.w("FavoritesViewModel", "User not authenticated, skipping Firestore load")
                    return@launch
                }

                val firestore = Firebase.firestore
                val snapshot = firestore.collection("users").document(user.uid)
                    .collection("recipes")
                    .get()
                    .await()

                // Clear existing local favorites to ensure it only shows what's in Firestore
                repository.deleteAllFavoriteRecipes()

                snapshot.documents.forEach { doc ->
                    try {
                        val name = doc.getString("name") ?: ""
                        val recipe = Recipe(
                            id = 0,
                            name = name,
                            prepTime = doc.getString("prepTime") ?: "",
                            cookTime = doc.getString("cookTime") ?: "",
                            calories = (doc.getLong("calories") ?: 0).toInt(),
                            matchPercentage = 0,
                            matchNote = "",
                            ingredients = "[]",
                            steps = "[]",
                            imageUrl = "",
                            cuisine = doc.getString("cuisine") ?: "",
                            flavorProfile = doc.getString("flavorProfile") ?: "",
                            isFavorite = true,
                            savedAt = (doc.getLong("timestamp") ?: System.currentTimeMillis())
                        )
                        viewModelScope.launch {
                            repository.insertRecipe(recipe)
                        }
                    } catch (e: Exception) {
                        Log.e("FavoritesViewModel", "Error syncing recipe doc: ${doc.id}", e)
                    }
                }

                Log.d("FavoritesViewModel", "Sync from Firestore completed")
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Failed to load recipes from Firestore", e)
            }
        }
    }

    fun loadRecipeFromStorage(recipe: Recipe) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val user = Firebase.auth.currentUser ?: throw Exception("User not authenticated")
                val firestore = Firebase.firestore

                // Find the Firestore doc to get the storagePath
                val snapshot = firestore.collection("users").document(user.uid)
                    .collection("recipes")
                    .whereEqualTo("name", recipe.name)
                    .get()
                    .await()

                val doc = snapshot.documents.firstOrNull()
                    ?: throw Exception("Recipe metadata not found in Firestore")

                val storagePath = doc.getString("storagePath")
                    ?: throw Exception("No storagePath in metadata")

                Log.d("FavoritesViewModel", "Loading JSON from Storage: $storagePath")

                // Download JSON from Firebase Storage
                val storage = Firebase.storage("gs://flavorquest-b35d8.firebasestorage.app")
                val storageRef = storage.reference.child(storagePath)
                val maxSize: Long = 1024 * 1024 // 1MB
                val bytes = storageRef.getBytes(maxSize).await()
                val json = String(bytes)

                // Parse JSON into Recipe
                val fullRecipe = Gson().fromJson(json, Recipe::class.java)
                Log.d("FavoritesViewModel", "Recipe loaded from Storage: ${fullRecipe.name}")

                _uiState.value = _uiState.value.copy(
                    selectedRecipe = fullRecipe,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Failed to load recipe from Storage", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load recipe: ${e.message}"
                )
            }
        }
    }

    fun selectRestaurantFromFavorites(restaurant: Restaurant) {
        _uiState.value = _uiState.value.copy(selectedRestaurant = restaurant)
    }

    fun clearSelectedRestaurant() {
        _uiState.value = _uiState.value.copy(selectedRestaurant = null)
    }

    fun clearSelectedRecipe() {
        _uiState.value = _uiState.value.copy(selectedRecipe = null)
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun removeRecipeFromFavorites(recipe: Recipe) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val user = Firebase.auth.currentUser ?: throw Exception("User not authenticated")
                val firestore = Firebase.firestore
                val storage = Firebase.storage("gs://flavorquest-b35d8.firebasestorage.app")

                // 1. Find the Firestore document to get storagePath
                val snapshot = firestore.collection("users").document(user.uid)
                    .collection("recipes")
                    .whereEqualTo("name", recipe.name)
                    .get()
                    .await()

                val doc = snapshot.documents.firstOrNull()
                if (doc != null) {
                    val storagePath = doc.getString("storagePath")
                    
                    // 2. Delete from Storage if path exists
                    if (storagePath != null) {
                        try {
                            storage.reference.child(storagePath).delete().await()
                            Log.d("FavoritesViewModel", "Deleted from Storage: $storagePath")
                        } catch (e: Exception) {
                            Log.e("FavoritesViewModel", "Failed to delete from Storage", e)
                        }
                    }

                    // 3. Delete from Firestore
                    doc.reference.delete().await()
                    Log.d("FavoritesViewModel", "Deleted from Firestore: ${doc.id}")
                }

                // 4. Delete from local Room DB
                repository.deleteRecipe(recipe)
                Log.d("FavoritesViewModel", "Deleted from local DB: ${recipe.name}")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Recipe removed successfully"
                )
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Failed to remove recipe", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to remove recipe: ${e.message}"
                )
            }
        }
    }

    fun removeRestaurantFromFavorites(restaurant: Restaurant) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val user = Firebase.auth.currentUser ?: throw Exception("User not authenticated")
                val firestore = Firebase.firestore

                // 1. Delete from Firestore
                val docId = restaurant.placeId.ifBlank { restaurant.name }
                firestore.collection("users").document(user.uid)
                    .collection("restaurants")
                    .document(docId)
                    .delete()
                    .await()
                Log.d("FavoritesViewModel", "Deleted restaurant from Firestore: $docId")

                // 2. Delete from local Room DB
                repository.deleteRestaurant(restaurant)
                Log.d("FavoritesViewModel", "Deleted restaurant from local DB: ${restaurant.name}")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Restaurant removed successfully"
                )
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Failed to remove restaurant", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to remove restaurant: ${e.message}"
                )
            }
        }
    }
}