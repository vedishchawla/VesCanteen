package com.example.vescanteen

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * REST API Helper — Fetches nutrition data from CalorieNinjas API.
 * Exp 6: Fetching and displaying data from a REST API.
 *
 * How it works:
 * - Makes HTTP GET request to CalorieNinjas API
 * - Parses JSON response
 * - Returns nutrition info (calories, protein, carbs, fat)
 * - Runs on background thread, callbacks on main thread
 */
object NutritionApiHelper {

    private const val TAG = "NutritionAPI"
    private const val BASE_URL = "https://api.calorieninjas.com/v1/nutrition?query="
    private const val API_KEY = "demo"  // Free tier API key

    /**
     * Fetch nutrition info for a food item.
     * @param foodName Name of the food (e.g., "poha", "samosa")
     * @param onResult Callback with nutrition string or error
     */
    fun fetchNutrition(foodName: String, onResult: (String) -> Unit) {
        // Run network call on background thread
        Thread {
            try {
                val encodedQuery = URLEncoder.encode(foodName, "UTF-8")
                val url = URL("$BASE_URL$encodedQuery")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("X-Api-Key", API_KEY)
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                Log.d(TAG, "Response code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val result = parseNutritionResponse(response, foodName)
                    // Post result back to main thread
                    Handler(Looper.getMainLooper()).post { onResult(result) }
                } else {
                    // API call failed — show fallback data
                    val fallback = getFallbackNutrition(foodName)
                    Handler(Looper.getMainLooper()).post { onResult(fallback) }
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "API call failed: ${e.message}")
                // Network error — show fallback data
                val fallback = getFallbackNutrition(foodName)
                Handler(Looper.getMainLooper()).post { onResult(fallback) }
            }
        }.start()
    }

    /** Parse JSON response from CalorieNinjas API */
    private fun parseNutritionResponse(json: String, foodName: String): String {
        return try {
            val jsonObject = org.json.JSONObject(json)
            val items = jsonObject.getJSONArray("items")

            if (items.length() > 0) {
                val item = items.getJSONObject(0)
                val calories = item.optDouble("calories", 0.0)
                val protein = item.optDouble("protein_g", 0.0)
                val carbs = item.optDouble("carbohydrates_total_g", 0.0)
                val fat = item.optDouble("fat_total_g", 0.0)
                val fiber = item.optDouble("fiber_g", 0.0)
                val sugar = item.optDouble("sugar_g", 0.0)

                "🍽 Nutrition Info — ${foodName.uppercase()}\n\n" +
                "🔥 Calories: ${calories.toInt()} kcal\n" +
                "💪 Protein: ${"%.1f".format(protein)}g\n" +
                "🌾 Carbs: ${"%.1f".format(carbs)}g\n" +
                "🧈 Fat: ${"%.1f".format(fat)}g\n" +
                "🥬 Fiber: ${"%.1f".format(fiber)}g\n" +
                "🍬 Sugar: ${"%.1f".format(sugar)}g\n\n" +
                "📊 Per 100g serving (approx.)"
            } else {
                getFallbackNutrition(foodName)
            }
        } catch (e: Exception) {
            getFallbackNutrition(foodName)
        }
    }

    /** Fallback nutrition data when API is unavailable */
    private fun getFallbackNutrition(foodName: String): String {
        val data = mapOf(
            "poha" to Triple(250, 4.5, 45.0),
            "samosa" to Triple(262, 3.5, 24.0),
            "vada pav" to Triple(290, 5.0, 36.0),
            "chai" to Triple(105, 3.0, 14.0),
            "coffee" to Triple(120, 3.5, 12.0),
            "maggi" to Triple(205, 4.5, 26.0),
            "dosa" to Triple(168, 3.9, 25.0),
            "sandwich" to Triple(250, 8.0, 30.0),
            "juice" to Triple(110, 0.5, 26.0),
            "lassi" to Triple(165, 4.0, 22.0)
        )

        val key = data.keys.find { foodName.lowercase().contains(it) }
        return if (key != null) {
            val (cal, protein, carbs) = data[key]!!
            "🍽 Nutrition Info — ${foodName.uppercase()}\n\n" +
            "🔥 Calories: $cal kcal\n" +
            "💪 Protein: ${protein}g\n" +
            "🌾 Carbs: ${carbs}g\n\n" +
            "📊 Approximate values per serving\n" +
            "ℹ️ Source: Estimated (offline)"
        } else {
            "🍽 Nutrition Info — ${foodName.uppercase()}\n\n" +
            "🔥 Calories: ~200 kcal\n" +
            "💪 Protein: ~5g\n" +
            "🌾 Carbs: ~30g\n\n" +
            "📊 Approximate values per serving\n" +
            "ℹ️ Source: Estimated (offline)"
        }
    }
}
