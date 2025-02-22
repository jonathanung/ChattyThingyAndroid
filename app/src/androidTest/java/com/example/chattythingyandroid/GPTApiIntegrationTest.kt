// File: app/src/androidTest/java/com/example/chattythingyandroid/utils/GPTApiIntegrationTest.kt
package com.example.chattythingyandroid

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chattythingyandroid.utils.getGptResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GPTApiIntegrationTest {

    @Test
    fun testGetGptResponseWithRealApi() = runBlocking {
        // WARNING: This test calls the actual API.
        // Ensure you have a valid API key and are aware of potential costs.
        val apiKey = BuildConfig.OPEN_AI_API_KEY  // Make sure this is set up correctly
        val response = getGptResponse("Hello, GPT!", apiKey)
        // In a real API call, you might get a non-null response
        assertNotNull("The GPT API should return a non-null response", response)
    }
}