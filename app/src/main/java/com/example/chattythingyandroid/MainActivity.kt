package com.example.chattythingyandroid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.lifecycle.lifecycleScope
import com.example.chattythingyandroid.models.Message
import com.example.chattythingyandroid.ui.screens.ChatScreen
import com.example.chattythingyandroid.utils.getGptResponse
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Use a stateful list for messages. For production apps, consider using ViewModel & StateFlow.
    private val messages = mutableListOf<Message>()
    private lateinit var database: DatabaseReference

    private val openAiApiKey = BuildConfig.OPEN_AI_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference.child("messages")
        setupFirebaseListener()

        setContent {
            MaterialTheme {
                ChatScreen(
                    messages = messages,
                    onSendMessage = { text ->
                        // Push user's message to Firebase
                        sendMessage(text)
                        // Call the GPT API and send its response
                        lifecycleScope.launch {
                            try {
                                val reply = getGptResponse(text, BuildConfig.OPEN_AI_API_KEY)
                                if (reply != null) {
                                    sendMessage(reply)
                                } else {
                                    Log.e("MainActivity", "Received null reply from GPT API")
                                }
                            } catch (e: Exception) {
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
                if (message != null) {
                    messages.add(message)
                    // In a production app, you'd use a reactive state (e.g., MutableStateFlow)
                    // to trigger recomposition of your Compose UI.
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

    private fun sendMessage(text: String) {
        val messageId = database.push().key ?: return
        val message = Message(id = messageId, text = text)
        database.child(messageId).setValue(message)
    }
}
