package com.example.chattythingyandroid.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    val model: String = "gpt-3.5-turbo",
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

suspend fun getGptResponse(userMessage: String, apiKey: String): String? {
    val messages = listOf(ChatMessage(role = "user", content = userMessage))
    val chatRequest = ChatRequest(messages = messages)
    val jsonBody = Json.encodeToString(chatRequest)
    val mediaType = "application/json".toMediaType()
    val requestBody = jsonBody.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://api.openai.com/v1/chat/completions")
        .addHeader("Authorization", "Bearer $apiKey")
        .post(requestBody)
        .build()

    val client = OkHttpClient()

    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                null
            } else {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val chatResponse = Json.decodeFromString<ChatResponse>(responseBody)
                    chatResponse.choices.firstOrNull()?.message?.content
                } else null
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
