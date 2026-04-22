package edu.utap.flavorquest.data.ai

import android.util.Log
import edu.utap.flavorquest.data.model.Recipe
import edu.utap.flavorquest.data.model.Restaurant
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RecipeRequest(
    val ingredients: String,
    val allergies: String,
    val time: String,
    val calories: String
)

data class RecipeResponse(
    val recipe_name: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val cooking_time: String,
    val calories: String
)

class AIService(private val apiKey: String = "AIzaSyATLJ-12MciKlOe4psDw_WI_vj-e1x5A18") {

    private val placesApiKey = "AIzaSyDA1KobvVipkAdL84bY0TCDVmia7x2maWI"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun getRecipeSuggestions(prompt: String): List<Recipe> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext getSampleRecipes()
        }

        try {
            val response = callAI(prompt)
            parseRecipesFromResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            getSampleRecipes()
        }
    }

    suspend fun getRestaurantSuggestions(prompt: String): List<Restaurant> =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext getSampleRestaurants()
            }

            try {
                val response = callAI(prompt)
                parseRestaurantsFromResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                getSampleRestaurants()
            }
        }

    /**
     * Search restaurants using Google Places API (New) Text Search.
     */
    suspend fun searchRestaurantsViaPlaces(query: String): List<Restaurant> =
        withContext(Dispatchers.IO) {
            try {
                val url = "https://places.googleapis.com/v1/places:searchText"

                val jsonBody = JSONObject().apply {
                    put("textQuery", query)
                    put("includedType", "restaurant")
                    put("maxResultCount", 5)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Goog-Api-Key", placesApiKey)
                    .addHeader(
                        "X-Goog-FieldMask",
                        "places.id,places.displayName,places.formattedAddress,places.rating," +
                                "places.priceLevel,places.nationalPhoneNumber,places.location," +
                                "places.photos,places.types,places.websiteUri"
                    )
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                Log.d("AIService", "Places API Response: $responseBody")

                if (!response.isSuccessful) {
                    Log.e("AIService", "Places API failed with code ${response.code}: $responseBody")
                    return@withContext getSampleRestaurants()
                }

                val jsonResponse = JSONObject(responseBody)
                val placesArray = jsonResponse.optJSONArray("places")
                    ?: return@withContext getSampleRestaurants()

                val restaurants = mutableListOf<Restaurant>()
                for (i in 0 until placesArray.length()) {
                    val place = placesArray.getJSONObject(i)
                    restaurants.add(parsePlaceToRestaurant(place))
                }

                if (restaurants.isEmpty()) getSampleRestaurants() else restaurants
            } catch (e: Exception) {
                Log.e("AIService", "Places API error: ${e.message}", e)
                getSampleRestaurants()
            }
        }

    private fun parsePlaceToRestaurant(place: JSONObject): Restaurant {
        val displayName = place.optJSONObject("displayName")?.optString("text", "Unknown") ?: "Unknown"
        val address = place.optString("formattedAddress", "")
        val rating = place.optDouble("rating", 0.0).toFloat()
        val phone = place.optString("nationalPhoneNumber", "")
        val websiteUrl = place.optString("websiteUri", "")
        val placeId = place.optString("id", "")

        // Parse price level
        val priceLevelStr = place.optString("priceLevel", "")
        val priceLevel = when (priceLevelStr) {
            "PRICE_LEVEL_FREE" -> 0
            "PRICE_LEVEL_INEXPENSIVE" -> 1
            "PRICE_LEVEL_MODERATE" -> 2
            "PRICE_LEVEL_EXPENSIVE" -> 3
            "PRICE_LEVEL_VERY_EXPENSIVE" -> 4
            else -> 2
        }

        // Parse location
        val location = place.optJSONObject("location")
        val latitude = location?.optDouble("latitude", 0.0) ?: 0.0
        val longitude = location?.optDouble("longitude", 0.0) ?: 0.0

        // Parse photo URL (first photo)
        var imageUrl = ""
        val photos = place.optJSONArray("photos")
        if (photos != null && photos.length() > 0) {
            val photoName = photos.getJSONObject(0).optString("name", "")
            if (photoName.isNotBlank()) {
                imageUrl = "https://places.googleapis.com/v1/$photoName/media?maxHeightPx=400&maxWidthPx=400&key=$placesApiKey"
            }
        }

        // Parse types to determine cuisine and type
        val typesArray = place.optJSONArray("types")
        val types = mutableListOf<String>()
        if (typesArray != null) {
            for (j in 0 until typesArray.length()) {
                types.add(typesArray.getString(j))
            }
        }
        val cuisine = types.firstOrNull { it !in listOf("restaurant", "food", "point_of_interest", "establishment") }
            ?.replace("_", " ")
            ?.replaceFirstChar { it.uppercase() }
            ?: "Restaurant"
        val type = if ("bar" in types) "Bar" else if ("cafe" in types) "Cafe" else "Restaurant"

        return Restaurant(
            name = displayName,
            rating = rating,
            distance = "",
            priceLevel = priceLevel,
            cuisine = cuisine,
            type = type,
            amenities = "",
            imageUrl = imageUrl,
            address = address,
            phone = phone,
            websiteUrl = websiteUrl,
            latitude = latitude,
            longitude = longitude,
            placeId = placeId
        )
    }

    suspend fun refineResults(prompt: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext "Here are refined suggestions based on your preferences. Try adjusting your filters for more options!"
        }

        try {
            callAI(prompt)
        } catch (e: Exception) {
            "Unable to refine results. Please try again."
        }
    }

    suspend fun getChefRecipeSuggestion(request: RecipeRequest): RecipeResponse? = withContext(Dispatchers.IO) {
        val prompt = """  
            You are a professional chef AI.  
   
            Suggest a recipe based on:  
            - Ingredients: ${request.ingredients}  
            - Allergies to avoid: ${request.allergies}  
            - Max cooking time: ${request.time}  
            - Target calories: ${request.calories}  
   
            Return response in JSON format:  
            {  
              "recipe_name": "",  
              "ingredients": [],  
              "steps": [],  
              "cooking_time": "",  
              "calories": ""  
            }  
        """.trimIndent()

        try {
            val response = callAI(prompt)
            gson.fromJson(response, RecipeResponse::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun callAI(prompt: String): String {
        val systemInstruction = "You are FlavorQuest, an AI culinary companion. Provide helpful food and restaurant recommendations. When suggesting recipes, DO NOT include any pricing or ratings. Return only valid JSON when requested."

        Log.d("AIService", "Prompt: $prompt")

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemInstruction\n\nUser request: $prompt")
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        Log.d("AIService", "Response Body: $responseBody")

        if (!response.isSuccessful) {
            throw Exception("Gemini API call failed with code ${response.code}: $responseBody")
        }

        val jsonResponse = JSONObject(responseBody)

        if (!jsonResponse.has("candidates")) {
            throw Exception("Gemini API response missing 'candidates' field: $responseBody")
        }

        val candidates = jsonResponse.getJSONArray("candidates")
        if (candidates.length() == 0) {
            throw Exception("Gemini API returned no candidates: $responseBody")
        }

        val extractedText = candidates
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        Log.d("AIService", "Extracted Text: $extractedText")

        return extractedText
    }

    private fun parseRecipesFromResponse(response: String): List<Recipe> {
        return try {
            val jsonArray = JSONArray(response)
            val recipes = mutableListOf<Recipe>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                recipes.add(
                    Recipe(
                        name = obj.getString("name"),
                        prepTime = obj.optString("prepTime", "10 mins"),
                        cookTime = obj.optString("cookTime", "20 mins"),
                        calories = obj.optInt("calories", 300),
                        matchPercentage = obj.optInt("matchPercentage", 90),
                        ingredients = obj.getJSONArray("ingredients").toString(),
                        steps = obj.getJSONArray("steps").toString(),
                        cuisine = obj.optString("cuisine", ""),
                        flavorProfile = obj.optString("flavorProfile", ""),
                        imageUrl = obj.optString("imageUrl", "")
                    )
                )
            }
            recipes
        } catch (e: Exception) {
            Log.e("AIService", "Error parsing recipes: ${e.message}")
            getSampleRecipes()
        }
    }

    private fun parseRestaurantsFromResponse(response: String): List<Restaurant> {
        return try {
            val listType = object : TypeToken<List<Restaurant>>() {}.type
            gson.fromJson(response, listType)
        } catch (e: Exception) {
            getSampleRestaurants()
        }
    }

    companion object {
        fun getSampleRecipes(): List<Recipe> = listOf(
            Recipe(
                id = 1,
                name = "Spicy Chicken Stir Fry",
                prepTime = "10 mins",
                cookTime = "15 mins",
                calories = 320,
                matchPercentage = 100,
                matchNote = "",
                ingredients = "[\"chicken breast\",\"bell peppers\",\"soy sauce\",\"garlic\",\"ginger\",\"chili flakes\",\"sesame oil\",\"rice\"]",
                steps = "[\"Slice chicken into strips\",\"Heat sesame oil in wok\",\"Stir-fry chicken until golden\",\"Add vegetables and stir-fry 3 mins\",\"Add sauce and cook 2 mins\",\"Serve over rice\"]",
                imageUrl = "",
                cuisine = "Asian",
                flavorProfile = "Spicy"
            ),
            Recipe(
                id = 2,
                name = "Garlic Tofu & Veggies",
                prepTime = "5 mins",
                cookTime = "12 mins",
                calories = 250,
                matchPercentage = 80,
                matchNote = "Need Soy Sauce",
                ingredients = "[\"firm tofu\",\"broccoli\",\"carrots\",\"garlic\",\"soy sauce\",\"sesame seeds\",\"olive oil\"]",
                steps = "[\"Press and cube tofu\",\"Heat oil in pan\",\"Pan-fry tofu until crispy\",\"Add garlic and vegetables\",\"Season with soy sauce\",\"Top with sesame seeds\"]",
                imageUrl = "",
                cuisine = "Asian",
                flavorProfile = "Savory"
            ),
            Recipe(
                id = 3,
                name = "Caprese Chicken Bake",
                prepTime = "10 mins",
                cookTime = "25 mins",
                calories = 380,
                matchPercentage = 75,
                matchNote = "",
                ingredients = "[\"chicken breast\",\"fresh mozzarella\",\"tomatoes\",\"basil\",\"balsamic glaze\",\"olive oil\",\"salt\",\"pepper\"]",
                steps = "[\"Preheat oven to 400°F\",\"Season chicken with salt and pepper\",\"Place in baking dish\",\"Top with tomato slices and mozzarella\",\"Bake 25 minutes\",\"Drizzle with balsamic glaze and basil\"]",
                imageUrl = "",
                cuisine = "Italian",
                flavorProfile = "Fresh"
            ),
            Recipe(
                id = 4,
                name = "Lemon Herb Salmon",
                prepTime = "5 mins",
                cookTime = "20 mins",
                calories = 350,
                matchPercentage = 85,
                matchNote = "",
                ingredients = "[\"salmon fillet\",\"lemon\",\"dill\",\"garlic\",\"olive oil\",\"salt\",\"pepper\",\"asparagus\"]",
                steps = "[\"Preheat oven to 375°F\",\"Place salmon on baking sheet\",\"Drizzle with olive oil and lemon juice\",\"Season with dill, garlic, salt and pepper\",\"Arrange asparagus around salmon\",\"Bake 18-20 minutes\"]",
                imageUrl = "",
                cuisine = "Mediterranean",
                flavorProfile = "Light"
            ),
            Recipe(
                id = 5,
                name = "Black Bean Tacos",
                prepTime = "10 mins",
                cookTime = "10 mins",
                calories = 290,
                matchPercentage = 70,
                matchNote = "",
                ingredients = "[\"black beans\",\"corn tortillas\",\"avocado\",\"lime\",\"cilantro\",\"red onion\",\"cumin\",\"salsa\"]",
                steps = "[\"Heat and season black beans with cumin\",\"Warm tortillas in a dry pan\",\"Dice avocado and red onion\",\"Assemble tacos with beans and toppings\",\"Squeeze lime and add cilantro\",\"Serve with salsa\"]",
                imageUrl = "",
                cuisine = "Mexican",
                flavorProfile = "Savory"
            )
        )

        fun getSampleRestaurants(): List<Restaurant> = listOf(
            Restaurant(
                id = 1,
                name = "Cantina Fiesta",
                rating = 4.0f,
                distance = "0.8 mi",
                priceLevel = 2,
                cuisine = "Mexican",
                type = "Restaurant",
                amenities = "Casual, Outdoor Patio",
                outdoorPatio = true
            ),
            Restaurant(
                id = 2,
                name = "El Rio Cantina",
                rating = 4.5f,
                distance = "1.2 mi",
                priceLevel = 3,
                cuisine = "Mexican",
                type = "Restaurant",
                amenities = "Live Music",
                liveMusic = true
            ),
            Restaurant(
                id = 3,
                name = "Taco Tavern",
                rating = 4.2f,
                distance = "2.5 mi",
                priceLevel = 2,
                cuisine = "Mexican",
                type = "Pub",
                amenities = "Craft Beer",
                craftBeer = true
            )
        )
    }
}