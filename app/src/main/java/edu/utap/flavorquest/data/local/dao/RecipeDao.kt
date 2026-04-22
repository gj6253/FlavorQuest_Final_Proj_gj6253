package edu.utap.flavorquest.data.local.dao

import androidx.room.*
import edu.utap.flavorquest.data.model.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY savedAt DESC")
    fun getFavoriteRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes ORDER BY savedAt DESC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Query("SELECT * FROM recipes WHERE name = :name LIMIT 1")
    suspend fun getRecipeByName(name: String): Recipe?

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): Recipe?

    @Query("DELETE FROM recipes WHERE isFavorite = 1")
    suspend fun deleteAllFavorites()
}
