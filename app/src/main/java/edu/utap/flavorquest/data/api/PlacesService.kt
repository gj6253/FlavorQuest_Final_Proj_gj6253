// app/src/main/java/edu/utap/flavorquest/data/api/PlacesService.kt

package edu.utap.flavorquest.data.api

import android.util.Log
import edu.utap.flavorquest.data.model.OrderOutProfile
import edu.utap.flavorquest.data.model.MoodType
import edu.utap.flavorquest.data.model.Restaurant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PlacesService(
    private val apiKey: String = "AIzaSyDA1KobvVipkAdL84bY0TCDVmia7x2maWI"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Build a natural language query from user preferences.
     * e.g. "spicy Mexican fine dining near me"
     */
    fun buildSearchQuery(mood: MoodType?, profile: OrderOutProfile): String {
        val parts = mutableListOf<String>()

        // Add mood-based flavor
        mood?.let {
            when (it) {
                MoodType.HEALTHY_LIGHT -> parts.add("healthy light")
                MoodType.SPICY_KICK -> parts.add("spicy")
                MoodType.COMFORT -> parts.add("comfort food cozy")
                MoodType.CHEAT_MEAL -> parts.add("indulgent popular")
                MoodType.ADVENTURE -> parts.add("unique exotic adventurous")
            }
        }

        // Add restaurant styles (cuisines)
        if (profile.restaurantStyles.isNotEmpty()) {
            parts.add(profile.restaurantStyles.joinToString(" "))
        }

        // Add restaurant types
        if (profile.restaurantTypes.isNotEmpty()) {
            parts.add(profile.restaurantTypes.joinToString(" "))
        }

        // Add price context
        when (profile.priceLevel) {
            1 -> parts.add("cheap budget")
            2 -> parts.add("moderate")
            3 -> parts.add("upscale")
            4 -> parts.add("luxury fine dining")
        }

        parts.add("restaurant")

        val query = parts.joinToString(" ")
        Log.d("PlacesService", "Built search query: $query")
        return query
    }

    /**
     * Search restaurants using Google Places API (New) Text Search.
     * https://developers.google.com/maps/documentation/places/web-service/text-search
     */
    suspend fun searchRestaurants(query: String): List<Restaurant> = withContext(Dispatchers.IO) {
        try {
            val url = "https://places.googleapis.com/v1/places:searchText"

            val requestBody = JSONObject().apply {
                put("textQuery", query)
                put("maxResultCount", 10)
            }

            Log.d("PlacesService", "Searching: $query")
            Log.d("PlacesService", "Request body: $requestBody")

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Goog-Api-Key", apiKey)
                .addHeader(
                    "X-Goog-FieldMask",
                    "places.displayName,places.formattedAddress,places.nationalPhoneNumber,places.rating,places.priceLevel,places.primaryType,places.photos"
                )
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            Log.d("PlacesService", "Response code: ${response.code}")
            Log.d("PlacesService", "Response body: $responseBody")

            if (!response.isSuccessful) {
                Log.e("PlacesService", "API call failed: ${response.code} - $responseBody")
                return@withContext emptyList()
            }

            val json = JSONObject(responseBody)
            val places = json.optJSONArray("places") ?: return@withContext emptyList()

            val restaurants = mutableListOf<Restaurant>()
            for (i in 0 until places.length()) {
                val place = places.getJSONObject(i)

                val name = place.optJSONObject("displayName")?.optString("text", "Unknown") ?: "Unknown"
                val address = place.optString("formattedAddress", "")
                val phone = place.optString("nationalPhoneNumber", "")
                val rating = place.optDouble("rating", 0.0).toFloat()
                val priceLevelStr = place.optString("priceLevel", "")
                val priceLevel = parsePriceLevel(priceLevelStr)
                val primaryType = place.optString("primaryType", "restaurant")

                // Get photo URL if available
                val photoUrl = getPhotoUrl(place)

                restaurants.add(
                    Restaurant(
                        name = name,
                        rating = rating,
                        distance = "Unknown",
                        priceLevel = priceLevel,
                        cuisine = primaryType.replace("_", " ").replaceFirstChar { it.uppercase() },
                        type = "Restaurant",
                        address = address,
                        imageUrl = photoUrl
                    )
                )
            }

            Log.d("PlacesService", "Parsed ${restaurants.size} restaurants")
            restaurants
        } catch (e: Exception) {
            Log.e("PlacesService", "Error searching restaurants", e)
            emptyList()
        }
    }

    private fun parsePriceLevel(level: String): Int {
        return when (level) {
            "PRICE_LEVEL_FREE" -> 1
            "PRICE_LEVEL_INEXPENSIVE" -> 1
            "PRICE_LEVEL_MODERATE" -> 2
            "PRICE_LEVEL_EXPENSIVE" -> 3
            "PRICE_LEVEL_VERY_EXPENSIVE" -> 4
            else -> 2 // default moderate
        }
    }

    private fun getPhotoUrl(place: JSONObject): String {
        val photos = place.optJSONArray("photos")
        if (photos != null && photos.length() > 0) {
            val photoName = photos.getJSONObject(0).optString("name", "")
            if (photoName.isNotBlank()) {
                return "https://places.googleapis.com/v1/$photoName/media?maxHeightPx=400&maxWidthPx=400&key=$apiKey"
            }
        }
        return ""
    }
}