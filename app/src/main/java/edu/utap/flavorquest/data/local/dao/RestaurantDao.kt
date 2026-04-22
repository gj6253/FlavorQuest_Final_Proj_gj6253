package edu.utap.flavorquest.data.local.dao

import androidx.room.*
import edu.utap.flavorquest.data.model.Restaurant
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants WHERE isFavorite = 1 ORDER BY savedAt DESC")
    fun getFavoriteRestaurants(): Flow<List<Restaurant>>

    @Query("SELECT * FROM restaurants ORDER BY savedAt DESC")
    fun getAllRestaurants(): Flow<List<Restaurant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: Restaurant): Long

    @Update
    suspend fun updateRestaurant(restaurant: Restaurant)

    @Delete
    suspend fun deleteRestaurant(restaurant: Restaurant)

    @Query("UPDATE restaurants SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("SELECT * FROM restaurants WHERE id = :id")
    suspend fun getRestaurantById(id: Long): Restaurant?
}
