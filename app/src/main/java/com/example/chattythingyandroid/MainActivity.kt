package com.example.chattythingyandroid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.example.chattythingyandroid.models.Message
import com.example.chattythingyandroid.ui.screens.ChatScreen
import com.example.chattythingyandroid.utils.getGptResponse
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateListOf

class MainActivity : ComponentActivity() {

    // Use mutableStateListOf so that Compose notices changes to the chat messages.
    private val messages = mutableStateListOf<Message>()
    private lateinit var database: DatabaseReference

    private val openAiApiKey = BuildConfig.OPEN_AI_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference.child("messages")
        setupFirebaseListener()

        setContent {
            MaterialTheme {
                // Define a state for showing "GPT is thinking..."
                val isGptThinking = remember { mutableStateOf(false) }
                
                ChatScreen(
                    messages = messages,
                    isGptThinking = isGptThinking.value,
                    onSendMessage = { text ->
                        // Generate a single key for the user's message
                        val userMessageId = database.push().key ?: return@ChatScreen
                        val userMessage = Message(
                            id = userMessageId,
                            text = text,
                            sender = "user",
                            timestamp = System.currentTimeMillis()
                        )
                        // Immediately add the user's message locally
                        messages.add(userMessage)
                        // Push the user's message to Firebase using the same key
                        sendMessage(userMessageId, text, "user")

                        // Set the thinking state before calling the GPT API.
                        isGptThinking.value = true

                        // Call the GPT API in the background.
                        lifecycleScope.launch {
                            try {
                                val reply = getGptResponse(text, openAiApiKey)
                                isGptThinking.value = false
                                if (reply != null) {
                                    // Generate a key for the bot's reply
                                    val botMessageId = database.push().key ?: return@launch
                                    val botMessage = Message(
                                        id = botMessageId,
                                        text = reply,
                                        sender = "bot",
                                        timestamp = System.currentTimeMillis()
                                    )
                                    // Immediately add the bot's reply locally
                                    messages.add(botMessage)
                                    // Push the bot's message to Firebase using the same key
                                    sendMessage(botMessageId, reply, "bot")
                                } else {
                                    Log.e("MainActivity", "Received null reply from GPT API")
                                }
                            } catch (e: Exception) {
                                isGptThinking.value = false
                                Log.e("MainActivity", "Error calling GPT API", e)
                            }
                        }
                    }
                )
            }
        }
    }

    private fun setupFirebaseListener() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                // Only add if this message's id is not already in our list.
                if (message != null && messages.none { it.id == message.id }) {
                    runOnUiThread {
                        messages.add(message)
                    }
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Database error: ${error.message}")
            }
        })
    }

    // Updated sendMessage to use a provided message ID.
    private fun sendMessage(messageId: String, text: String, sender: String = "user") {
        val message = Message(
            id = messageId,
            text = text,
            sender = sender,
            timestamp = System.currentTimeMillis()
        )
        database.child(messageId).setValue(message)
    }
}
