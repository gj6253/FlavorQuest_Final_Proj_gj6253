package edu.utap.flavorquest.data.ai

import edu.utap.flavorquest.data.model.CookingProfile
import edu.utap.flavorquest.data.model.MoodType
import edu.utap.flavorquest.data.model.OrderOutProfile

object PromptBuilder {

    fun buildRecipePrompt(
        mood: MoodType?,
        profile: CookingProfile
    ): String {
        val sb = StringBuilder()
        sb.appendLine("Generate recipe suggestions based on the following preferences:")
        sb.appendLine()

        mood?.let {
            sb.appendLine("Mood: ${it.label}")
        }

        if (profile.allergies.isNotBlank()) {
            sb.appendLine("Allergies/Restrictions: ${profile.allergies}")
        }

        if (profile.dietType.isNotBlank()) {
            sb.appendLine("Diet Type: ${profile.dietType}")
        }

        if (profile.ingredients.isNotEmpty()) {
            sb.appendLine("Available Ingredients: ${profile.ingredients.joinToString(", ")}")
        }

        sb.appendLine("Cooking Time: ${profile.cookingTimeMin}-${profile.cookingTimeMax} minutes")

        if (profile.calories.isNotBlank()) {
            sb.appendLine("Target Calories: ${profile.calories}")
        }

        if (profile.cuisinePreferences.isNotEmpty()) {
            sb.appendLine("Cuisine Preferences: ${profile.cuisinePreferences.joinToString(", ")}")
        }

        if (profile.flavorPreferences.isNotEmpty()) {
            sb.appendLine("Flavor Preferences: ${profile.flavorPreferences.joinToString(", ")}")
        }

        sb.appendLine()
        sb.appendLine("Please provide exactly 5 recipe suggestions with:")
        sb.appendLine("- Recipe name")
        sb.appendLine("- Prep time and cook time")
        sb.appendLine("- Estimated calories")
        sb.appendLine("- Match percentage based on the preferences")
        sb.appendLine("- cuisine")
        sb.appendLine("- flavorProfile")
        sb.appendLine()
        sb.appendLine("Format response as a JSON array where each object has these exact keys: 'name', 'prepTime', 'cookTime', 'calories' (as integer), 'matchPercentage' (as integer), 'ingredients' (as array of strings), 'steps' (as array of strings), 'cuisine', 'flavorProfile'.")

        return sb.toString()
    }

    fun buildRestaurantPrompt(
        mood: MoodType?,
        profile: OrderOutProfile
    ): String {
        val sb = StringBuilder()
        sb.appendLine("Suggest restaurants based on the following preferences:")
        sb.appendLine()

        mood?.let {
            sb.appendLine("Mood: ${it.label}")
        }

        if (profile.restaurantStyles.isNotEmpty()) {
            sb.appendLine("Restaurant Style: ${profile.restaurantStyles.joinToString(", ")}")
        }

        if (profile.restaurantTypes.isNotEmpty()) {
            sb.appendLine("Restaurant Type: ${profile.restaurantTypes.joinToString(", ")}")
        }

        sb.appendLine("Price Level: ${"$".repeat(profile.priceLevel)}")
        sb.appendLine("Minimum Rating: ${profile.minimumRating} stars")
        sb.appendLine("Search Radius: ${profile.searchRadius} miles")

        sb.appendLine()
        sb.appendLine("Please provide 3-5 restaurant suggestions with:")
        sb.appendLine("- Restaurant name")
        sb.appendLine("- Rating")
        sb.appendLine("- Distance")
        sb.appendLine("- Price level")
        sb.appendLine("- Cuisine type")
        sb.appendLine("- Amenities (outdoor patio, live music, etc.)")
        sb.appendLine()
        sb.appendLine("Format response as JSON array.")

        return sb.toString()
    }

    /**
     * Build a natural language query for Google Places Text Search API.
     */
    fun buildPlacesTextQuery(
        mood: MoodType?,
        profile: OrderOutProfile
    ): String {
        val parts = mutableListOf<String>()

        mood?.let { parts.add(it.label) }

        if (profile.restaurantStyles.isNotEmpty()) {
            parts.add(profile.restaurantStyles.joinToString(" "))
        }

        if (profile.restaurantTypes.isNotEmpty()) {
            parts.add(profile.restaurantTypes.joinToString(" "))
        }

        if (profile.additionalDetails.isNotBlank()) {
            parts.add(profile.additionalDetails.trim())
        }

        // Ensure "restaurant" is in the query if not already present
        val query = parts.joinToString(" ")
        return if (query.contains("restaurant", ignoreCase = true)) {
            query
        } else {
            "$query restaurant"
        }
    }

    fun buildRefinementPrompt(
        originalQuery: String,
        userMessage: String
    ): String {
        return """    
            |Based on the previous search:    
            |$originalQuery    
            |    
            |The user wants to refine their results:    
            |"$userMessage"    
            |    
            |Please provide updated suggestions based on this refinement.    
            |Format response as JSON array.    
        """.trimMargin()
    }
}