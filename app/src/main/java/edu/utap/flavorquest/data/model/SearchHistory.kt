package edu.utap.flavorquest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "",       // Firebase UID
    val title: String,
    val searchType: String,        // "recipe" or "restaurant"
    val queryDetails: String,      // Summary of search params
    val timestamp: Long = System.currentTimeMillis(),
    val filterSummary: String = "" // e.g. "Italian · $$$ · Patio"
)
