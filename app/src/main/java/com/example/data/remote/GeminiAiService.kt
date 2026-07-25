package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.PriceEstimate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

    suspend fun getChatResponse(userMessage: String, contextCategory: String? = null): String =
        withContext(Dispatchers.IO) {
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext getFallbackChatResponse(userMessage, contextCategory)
            }

            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val systemPrompt = "You are UrbanAI, an expert customer assistant for UrbanConnect home service booking app. Provide friendly, helpful, concise advice on home repairs, pricing estimates, booking steps, and finding top electricians, plumbers, carpenters, AC technicians, painters, cleaners, tutors, beauticians, appliance repair technicians, movers, and gardeners. Keep responses concise, clear and structured with bullet points where appropriate."

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", userMessage))
                            })
                        })
                    })
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", systemPrompt))
                        })
                    })
                }

                val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        val json = JSONObject(responseBody)
                        val candidates = json.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val text = parts.getJSONObject(0).optString("text", "")
                                if (text.isNotBlank()) return@withContext text
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GeminiAiService", "Error calling Gemini API: ${e.message}")
            }

            return@withContext getFallbackChatResponse(userMessage, contextCategory)
        }

    suspend fun estimatePrice(serviceCategory: String, details: String, isEmergency: Boolean): PriceEstimate =
        withContext(Dispatchers.IO) {
            if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    val prompt = """
                        You are an AI price estimation engine for UrbanConnect.
                        Category: $serviceCategory
                        Job Details: $details
                        Is Emergency: $isEmergency
                        
                        Return JSON format only:
                        {
                          "estimatedLow": 45.0,
                          "estimatedHigh": 85.0,
                          "recommendedDurationHours": 1.5,
                          "AIReasoning": "Explanation here",
                          "breakdownItems": ["Base inspection: $25", "Labor rate: $35/hr", "Emergency surcharge: $15"]
                        }
                    """.trimIndent()

                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                    val requestJson = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().put("text", prompt))
                                })
                            })
                        })
                    }

                    val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder().url(url).post(body).build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string() ?: ""
                            val json = JSONObject(responseBody)
                            val text = json.optJSONArray("candidates")?.optJSONObject(0)
                                ?.optJSONObject("content")?.optJSONArray("parts")
                                ?.optJSONObject(0)?.optString("text", "") ?: ""
                            
                            val jsonStart = text.indexOf("{")
                            val jsonEnd = text.lastIndexOf("}")
                            if (jsonStart != -1 && jsonEnd != -1) {
                                val parsed = JSONObject(text.substring(jsonStart, jsonEnd + 1))
                                val low = parsed.optDouble("estimatedLow", 50.0)
                                val high = parsed.optDouble("estimatedHigh", 90.0)
                                val dur = parsed.optDouble("recommendedDurationHours", 2.0).toFloat()
                                val reasoning = parsed.optString("AIReasoning", "Estimated based on category average and job complexity.")
                                val itemsJson = parsed.optJSONArray("breakdownItems")
                                val items = mutableListOf<String>()
                                if (itemsJson != null) {
                                    for (i in 0 until itemsJson.length()) {
                                        items.add(itemsJson.getString(i))
                                    }
                                }
                                return@withContext PriceEstimate(serviceCategory, low, high, dur, reasoning, items)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GeminiAiService", "Price estimation failed: ${e.message}")
                }
            }

            // Smart offline calculation fallback
            val baseLow = when (serviceCategory.lowercase()) {
                "electrician" -> 40.0
                "plumber" -> 45.0
                "ac technician" -> 55.0
                "painter" -> 120.0
                "cleaner" -> 35.0
                "tutor" -> 30.0
                "beautician" -> 50.0
                "appliance repair" -> 45.0
                "mover" -> 150.0
                "gardener" -> 40.0
                else -> 40.0
            }
            val emergencyMult = if (isEmergency) 1.4 else 1.0
            val low = baseLow * emergencyMult
            val high = (baseLow * 1.6) * emergencyMult

            PriceEstimate(
                category = serviceCategory,
                estimatedLow = low,
                estimatedHigh = high,
                recommendedDurationHours = 2.0f,
                AIReasoning = "UrbanAI Smart Estimate based on $serviceCategory market averages, standard labor rates, and ${if (isEmergency) "emergency priority dispatch" else "scheduled service time"}.",
                breakdownItems = listOf(
                    "Standard Diagnostic & Setup Fee: $${String.format("%.2f", low * 0.4)}",
                    "Certified Professional Labor (2 hrs): $${String.format("%.2f", low * 0.6)}",
                    if (isEmergency) "Emergency Instant Dispatch: $${String.format("%.2f", low * 0.35)}" else "Safety Inspection & Work Warranty: Included"
                )
            )
        }

    private fun getFallbackChatResponse(query: String, contextCategory: String?): String {
        val q = query.lowercase()
        return when {
            q.contains("price") || q.contains("cost") || q.contains("rate") || q.contains("estimate") ->
                "💡 **UrbanAI Price Insight**\n\nStandard rates on UrbanConnect:\n- Electricians: $35 - $65/hr\n- Plumbers: $40 - $75/hr\n- AC Technicians: $50 - $90/service\n- Deep Home Cleaning: $80 - $180\n- Emergency Bookings incur a 25% priority dispatch surcharge.\n\nYou can use our **Smart Price Estimator** tool above to get a customized AI breakdown!"

            q.contains("emergency") || q.contains("urgent") || q.contains("leak") || q.contains("spark") ->
                "🚨 **Emergency Service Guidance**\n\nFor immediate assistance:\n1. Switch to 'Emergency Booking' mode when placing a request.\n2. Nearby available technicians with top ratings (4.8+ ★) will be dispatched within 15-30 minutes.\n3. Turn off main water/power valves if you suspect active leaks or sparks!"

            q.contains("book") || q.contains("schedule") || q.contains("hire") ->
                "📅 **How to Book on UrbanConnect**\n\n1. Select your desired service category from the home screen.\n2. Pick a verified professional based on rating, distance, or hourly rate.\n3. Choose your preferred date and time slot.\n4. Apply any active coupons (e.g., URBAN100 for 10% off!).\n5. Confirm and track your technician live on the map!"

            q.contains("guarantee") || q.contains("verify") || q.contains("safe") || q.contains("warranty") ->
                "🛡️ **UrbanConnect Service Guarantee**\n\n- All professionals are 100% background-verified with government ID & skills certification.\n- Every booking includes $10,000 Property Damage Protection.\n- 30-Day Service Warranty on all completed repairs."

            else ->
                "Hello! I'm **UrbanAI**, your smart assistant on UrbanConnect. I can help you find verified electricians, plumbers, carpenters, cleaners, AC technicians, beauticians, or tutors nearby. How can I assist you with your home today?"
        }
    }
}
