package edu.utap.flavorquest.data.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val memberSince: String = "",
    val allergies: List<String> = emptyList(),
    val favoriteCuisines: List<String> = emptyList(),
    val dietaryPreferences: List<String> = emptyList(),
    val calorieGoal: Int = 0
)
