package edu.utap.flavorquest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val prepTime: String,
    val cookTime: String,
    val calories: Int,
    val matchPercentage: Int = 0,
    val matchNote: String = "",
    val ingredients: String,       // JSON list stored as string
    val steps: String,             // JSON list stored as string
    val imageUrl: String = "",
    val cuisine: String = "",
    val flavorProfile: String = "",
    val isFavorite: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)
