package edu.utap.flavorquest.data.local.dao

import androidx.room.*
import edu.utap.flavorquest.data.model.SearchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllHistory(userId: String): Flow<List<SearchHistory>>

    @Query("SELECT * FROM search_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(userId: String, limit: Int = 20): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: SearchHistory): Long

    @Delete
    suspend fun deleteHistory(history: SearchHistory)

    @Query("DELETE FROM search_history WHERE userId = :userId")
    suspend fun clearAllHistory(userId: String)

    @Query("SELECT * FROM search_history WHERE userId = :userId AND searchType = :type ORDER BY timestamp DESC")
    fun getHistoryByType(userId: String, type: String): Flow<List<SearchHistory>>
}
