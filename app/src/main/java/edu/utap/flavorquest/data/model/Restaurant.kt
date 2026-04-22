package edu.utap.flavorquest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rating: Float,
    val distance: String,
    val priceLevel: Int,           // 1-4 ($-$$$$)
    val cuisine: String,
    val type: String,              // Restaurant, Bar, Pub, etc.
    val amenities: String = "",    // Comma-separated: Casual, Outdoor Patio, etc.
    val imageUrl: String = "",
    val address: String = "",
    val phone: String = "",
    val websiteUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val placeId: String = "",
    val liveMusic: Boolean = false,
    val craftBeer: Boolean = false,
    val outdoorPatio: Boolean = false,
    val isFavorite: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)