package edu.utap.flavorquest.data.model

enum class MoodType(val label: String, val emoji: String) {
    HEALTHY_LIGHT("Healthy & Light", "\uD83E\uDD57"),
    SPICY_KICK("Spicy Kick", "\uD83C\uDF36\uFE0F"),
    COMFORT("Comfort", "\uD83C\uDF72"),
    CHEAT_MEAL("Cheat Meal", "\uD83C\uDF54"),
    ADVENTURE("Adventure", "\uD83C\uDF0E")
}

enum class CookingTab {
    COOK_AT_HOME,
    ORDER_OUT
}

data class CookingProfile(
    val allergies: String = "",
    val dietType: String = "",
    val ingredients: List<String> = emptyList(),
    val cookingTimeMin: Int = 30,
    val cookingTimeMax: Int = 60,
    val calories: String = "",
    val cuisinePreferences: List<String> = emptyList(),
    val flavorPreferences: List<String> = emptyList()
)

data class OrderOutProfile(
    val restaurantStyles: List<String> = emptyList(),
    val restaurantTypes: List<String> = emptyList(),
    val priceLevel: Int = 2,
    val minimumRating: Float = 3.0f,
    val searchRadius: Float = 5.0f,
    val additionalDetails: String = ""
)