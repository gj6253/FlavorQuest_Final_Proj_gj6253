package edu.utap.flavorquest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.utap.flavorquest.data.ai.AIService
import edu.utap.flavorquest.data.ai.PromptBuilder
import edu.utap.flavorquest.data.local.AppDatabase
import edu.utap.flavorquest.data.repository.FlavorQuestRepository
import edu.utap.flavorquest.data.model.CookingProfile
import edu.utap.flavorquest.data.model.CookingTab
import edu.utap.flavorquest.data.model.MoodType
import edu.utap.flavorquest.data.model.OrderOutProfile
import edu.utap.flavorquest.data.model.Recipe
import edu.utap.flavorquest.data.model.Restaurant
import edu.utap.flavorquest.data.model.SearchHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedMood: MoodType? = null,
    val selectedTab: CookingTab = CookingTab.COOK_AT_HOME,
    val cookingProfile: CookingProfile = CookingProfile(),
    val orderOutProfile: OrderOutProfile = OrderOutProfile(),
    val isLoading: Boolean = false,
    val recipes: List<Recipe> = emptyList(),
    val restaurants: List<Restaurant> = emptyList(),
    val error: String? = null,
    val navigateToResults: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.Companion.getDatabase(application)
    private val repository = FlavorQuestRepository(
        database.recipeDao(),
        database.restaurantDao(),
        database.searchHistoryDao()
    )
    private val aiService = AIService()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun selectMood(mood: MoodType) {
        val currentMood = _uiState.value.selectedMood
        val newMood = if (currentMood == mood) null else mood
        _uiState.value = _uiState.value.copy(selectedMood = newMood, error = null)
    }

    fun selectTab(tab: CookingTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab, error = null)
    }

    fun updateCookingProfile(profile: CookingProfile) {
        _uiState.value = _uiState.value.copy(cookingProfile = profile, error = null)
    }

    fun updateOrderOutProfile(profile: OrderOutProfile) {
        _uiState.value = _uiState.value.copy(orderOutProfile = profile, error = null)
    }

    fun generateSuggestions() {
        val state = _uiState.value

        // Validation: Check if ingredients (Cook at Home) are provided
        if (state.selectedTab == CookingTab.COOK_AT_HOME && state.cookingProfile.ingredients.isEmpty()) {
            _uiState.value = state.copy(error = "Please add at least one ingredient!")
            return
        }

        // Relaxed validation for Order Out: mood OR styles OR additionalDetails
        if (state.selectedTab == CookingTab.ORDER_OUT &&
            state.orderOutProfile.restaurantStyles.isEmpty() &&
            state.orderOutProfile.additionalDetails.isBlank()
        ) {
            _uiState.value = state.copy(error = "Please select a restaurant style or add details!")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                when (state.selectedTab) {
                    CookingTab.COOK_AT_HOME -> {
                        val prompt = PromptBuilder.buildRecipePrompt(
                            state.selectedMood,
                            state.cookingProfile
                        )
                        //val recipes = AIService.getSampleRecipes()
                        val recipes = aiService.getRecipeSuggestions(prompt)
                        // Save search history
                        repository.insertHistory(
                            SearchHistory(
                                title = recipes.firstOrNull()?.name ?: "Recipe Search",
                                searchType = "recipe",
                                queryDetails = "Recipe Search · Cook · ${
                                    state.cookingProfile.ingredients.joinToString(", ")
                                }",
                                filterSummary = buildRecipeFilterSummary(state)
                            )
                        )

                        _uiState.value = state.copy(
                            isLoading = false,
                            recipes = recipes,
                            navigateToResults = true
                        )
                    }

                    CookingTab.ORDER_OUT -> {
                        // Use Google Places Text Search API
                        val query = PromptBuilder.buildPlacesTextQuery(
                            state.selectedMood,
                            state.orderOutProfile
                        )
                        val restaurants = aiService.searchRestaurantsViaPlaces(query)

                        // Save search history
                        repository.insertHistory(
                            SearchHistory(
                                title = restaurants.firstOrNull()?.name ?: "Restaurant Search",
                                searchType = "restaurant",
                                queryDetails = "Restaurant Search · ${
                                    state.orderOutProfile.restaurantStyles.joinToString(", ")
                                }",
                                filterSummary = buildRestaurantFilterSummary(state)
                            )
                        )

                        _uiState.value = state.copy(
                            isLoading = false,
                            restaurants = restaurants,
                            navigateToResults = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to generate suggestions"
                )
            }
        }
    }

    fun resetSession() {
        _uiState.value = HomeUiState()
    }

    fun onNavigated() {
        _uiState.value = _uiState.value.copy(navigateToResults = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun buildRecipeFilterSummary(state: HomeUiState): String {
        val parts = mutableListOf<String>()
        state.selectedMood?.let { parts.add(it.label) }
        if (state.cookingProfile.dietType.isNotBlank()) {
            parts.add(state.cookingProfile.dietType)
        }
        if (state.cookingProfile.ingredients.isNotEmpty()) {
            parts.add(state.cookingProfile.ingredients.take(2).joinToString(", "))
        }
        return parts.joinToString(" · ")
    }

    private fun buildRestaurantFilterSummary(state: HomeUiState): String {
        val parts = mutableListOf<String>()
        state.selectedMood?.let { parts.add(it.label) }
        if (state.orderOutProfile.additionalDetails.isNotBlank()) {
            parts.add(state.orderOutProfile.additionalDetails)
        }
        if (state.orderOutProfile.restaurantStyles.isNotEmpty()) {
            parts.add(state.orderOutProfile.restaurantStyles.first())
        }
        parts.add("$".repeat(state.orderOutProfile.priceLevel))
        return parts.joinToString(" · ")
    }
}