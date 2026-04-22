package edu.utap.flavorquest.data.repository

import edu.utap.flavorquest.data.local.dao.RecipeDao
import edu.utap.flavorquest.data.local.dao.RestaurantDao
import edu.utap.flavorquest.data.local.dao.SearchHistoryDao
import edu.utap.flavorquest.data.model.Recipe
import edu.utap.flavorquest.data.model.Restaurant
import edu.utap.flavorquest.data.model.SearchHistory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class FlavorQuestRepository(
    private val recipeDao: RecipeDao,
    private val restaurantDao: RestaurantDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Recipes
    val favoriteRecipes: Flow<List<Recipe>> = recipeDao.getFavoriteRecipes()
    val allRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insertRecipe(recipe)
    suspend fun getRecipeByName(name: String): Recipe? = recipeDao.getRecipeByName(name)
    suspend fun deleteAllFavoriteRecipes() = recipeDao.deleteAllFavorites()
    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)
    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)
    suspend fun toggleRecipeFavorite(id: Long, isFavorite: Boolean) =
        recipeDao.updateFavoriteStatus(id, isFavorite)
    suspend fun getRecipeById(id: Long): Recipe? = recipeDao.getRecipeById(id)

    // Restaurants
    val favoriteRestaurants: Flow<List<Restaurant>> = restaurantDao.getFavoriteRestaurants()
    val allRestaurants: Flow<List<Restaurant>> = restaurantDao.getAllRestaurants()

    suspend fun insertRestaurant(restaurant: Restaurant): Long =
        restaurantDao.insertRestaurant(restaurant)
    suspend fun updateRestaurant(restaurant: Restaurant) =
        restaurantDao.updateRestaurant(restaurant)
    suspend fun deleteRestaurant(restaurant: Restaurant) =
        restaurantDao.deleteRestaurant(restaurant)
    suspend fun toggleRestaurantFavorite(id: Long, isFavorite: Boolean) =
        restaurantDao.updateFavoriteStatus(id, isFavorite)

    // Search History
    fun getAllHistory(userId: String): Flow<List<SearchHistory>> = searchHistoryDao.getAllHistory(userId)

    fun getRecentHistory(userId: String, limit: Int = 20): Flow<List<SearchHistory>> =
        searchHistoryDao.getRecentHistory(userId, limit)

    suspend fun insertHistory(history: SearchHistory): Long {
        val id = searchHistoryDao.insertHistory(history)
        if (history.userId.isNotEmpty()) {
            syncHistoryToFirestore(history.copy(id = id))
        }
        return id
    }

    private suspend fun syncHistoryToFirestore(history: SearchHistory) {
        try {
            val historyMap = hashMapOf(
                "userId" to history.userId,
                "title" to history.title,
                "searchType" to history.searchType,
                "queryDetails" to history.queryDetails,
                "timestamp" to history.timestamp,
                "filterSummary" to history.filterSummary
            )
            firestore.collection("search_history")
                .document("${history.userId}_${history.timestamp}")
                .set(historyMap)
                .await()
            
            cleanupOldHistory(history.userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun cleanupOldHistory(userId: String) {
        val sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        try {
            val oldDocs = firestore.collection("search_history")
                .whereEqualTo("userId", userId)
                .whereLessThan("timestamp", sevenDaysAgo)
                .get()
                .await()
            
            for (doc in oldDocs) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteHistory(history: SearchHistory) =
        searchHistoryDao.deleteHistory(history)

    suspend fun clearAllHistory(userId: String) = searchHistoryDao.clearAllHistory(userId)

    fun getHistoryByType(userId: String, type: String): Flow<List<SearchHistory>> =
        searchHistoryDao.getHistoryByType(userId, type)
}
