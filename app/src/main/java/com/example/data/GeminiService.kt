package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Queries Gemini 3.5 Flash for personalized educational responses.
     */
    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Empty or template API key found.")
            return@withContext "API_KEY_MISSING"
        }

        // Using standard gemini-3.5-flash as specified in guidelines
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        
        try {
            val jsonBody = JSONObject().apply {
                val contentsArr = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArr = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObj)
                        }
                        put("parts", partsArr)
                    }
                    put(contentObj)
                }
                put("contents", contentsArr)

                if (systemInstruction != null) {
                    val systemInstructionObj = JSONObject().apply {
                        val partsArr = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", systemInstruction)
                            }
                            put(partObj)
                        }
                        put("parts", partsArr)
                    }
                    put("systemInstruction", systemInstructionObj)
                }
            }

            val mediaType = "application/json".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""
            Log.d(TAG, "Gemini JSON Response: $bodyString")

            if (response.isSuccessful) {
                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No response text found.")
                        }
                    }
                }
                "Empty response from Gemini."
            } else {
                Log.e(TAG, "API call unsuccessful: ${response.code} - $bodyString")
                "Error generating study resources: ${response.code}. Please ensure your API Key is correctly configured in AI Studio Secrets tab."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini API call", e)
            "Error: ${e.localizedMessage ?: "Failed to connect to PrepNexus AI systems."}. Check network connection or configuration."
        }
    }
}
