package edu.utap.flavorquest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.flavorquest.data.model.SearchHistory
import edu.utap.flavorquest.data.ai.AIService
import edu.utap.flavorquest.data.local.AppDatabase
import edu.utap.flavorquest.data.model.Restaurant
import edu.utap.flavorquest.data.repository.FlavorQuestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class RestaurantUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val selectedRestaurant: Restaurant? = null,
    val isLoading: Boolean = false,
    val chatMessage: String = "",
    val error: String? = null
)

class RestaurantViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.Companion.getDatabase(application)
    private val repository = FlavorQuestRepository(
        database.recipeDao(),
        database.restaurantDao(),
        database.searchHistoryDao()
    )
    private val aiService = AIService()

    private val _uiState = MutableStateFlow(RestaurantUiState())
    val uiState: StateFlow<RestaurantUiState> = _uiState.asStateFlow()

    fun setRestaurants(restaurants: List<Restaurant>) {
        _uiState.value = _uiState.value.copy(restaurants = restaurants)
    }

    fun selectRestaurant(restaurant: Restaurant) {
        _uiState.value = _uiState.value.copy(selectedRestaurant = restaurant)
        saveToHistory(restaurant)
    }

    private fun saveToHistory(restaurant: Restaurant) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val history = SearchHistory(
                userId = user?.uid ?: "",
                title = restaurant.name,
                searchType = "restaurant",
                queryDetails = "${restaurant.cuisine} · ${restaurant.type}",
                filterSummary = "${restaurant.rating} ⭐ · ${"$".repeat(restaurant.priceLevel)} · ${restaurant.address}"
            )
            repository.insertHistory(history)
        }
    }

    fun saveRestaurant(restaurant: Restaurant) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val restaurantToSave = restaurant.copy(
                userId = user?.uid ?: "",
                isFavorite = true, 
                savedAt = System.currentTimeMillis()
            )
            repository.insertRestaurant(restaurantToSave)
            saveRestaurantToFirestore(restaurantToSave)
        }
    }

    private suspend fun saveRestaurantToFirestore(restaurant: Restaurant) {
        try {
            val user = FirebaseAuth.getInstance().currentUser ?: return
            val firestore = FirebaseFirestore.getInstance()
            
            val restaurantMap = hashMapOf(
                "name" to restaurant.name,
                "rating" to restaurant.rating,
                "distance" to restaurant.distance,
                "priceLevel" to restaurant.priceLevel,
                "cuisine" to restaurant.cuisine,
                "type" to restaurant.type,
                "amenities" to restaurant.amenities,
                "imageUrl" to restaurant.imageUrl,
                "address" to restaurant.address,
                "phone" to restaurant.phone,
                "websiteUrl" to restaurant.websiteUrl,
                "latitude" to restaurant.latitude,
                "longitude" to restaurant.longitude,
                "placeId" to restaurant.placeId,
                "liveMusic" to restaurant.liveMusic,
                "craftBeer" to restaurant.craftBeer,
                "outdoorPatio" to restaurant.outdoorPatio,
                "timestamp" to restaurant.savedAt
            )

            firestore.collection("users")
                .document(user.uid)
                .collection("restaurants")
                .document(restaurant.placeId.ifBlank { restaurant.name })
                .set(restaurantMap)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                val refinedRestaurants = aiService.searchRestaurantsViaPlaces(message)
                _uiState.value = _uiState.value.copy(
                    restaurants = refinedRestaurants,
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