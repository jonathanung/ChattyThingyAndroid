package com.example.chattythingyandroid.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatRequest(
    val model: String = "gpt-4",
    val messages: List<ChatMessage>
)

@Serializable
data class Choice(
    val message: ChatMessage
)

@Serializable
data class ChatResponse(
    val choices: List<Choice>
)

suspend fun getGptResponse(userMessage: String, apiKey: String): String? = withContext(Dispatchers.IO) {
    val messages = listOf(ChatMessage(role = "user", content = userMessage))
    val chatRequest = ChatRequest(messages = messages)
    
    // Configure JSON to encode default values and ignore unknown keys during decoding.
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    
    val jsonBody = json.encodeToString(chatRequest)
    val mediaType = "application/json".toMediaType()
    val requestBody = jsonBody.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://api.openai.com/v1/chat/completions")
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Content-Type", "application/json")
        .post(requestBody)
        .build()

    val client = OkHttpClient()

    try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                println("Error calling GPT API. Code: ${response.code}, Body: $errorBody")
                null
            } else {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrEmpty()) {
                    val chatResponse = json.decodeFromString<ChatResponse>(responseBody)
                    chatResponse.choices.firstOrNull()?.message?.content
                } else {
                    println("Received empty response body from GPT API")
                    null
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
