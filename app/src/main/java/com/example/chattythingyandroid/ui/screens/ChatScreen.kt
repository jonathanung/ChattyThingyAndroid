package com.example.chattythingyandroid.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chattythingyandroid.models.Message
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    messages: List<Message>,
    isGptThinking: Boolean,
    onSendMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Reverse the chat list so that new messages show at the bottom.
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = scrollState,
            reverseLayout = true,
            contentPadding = PaddingValues(8.dp)
        ) {
            // Reverse the list so that the newest messages appear at the bottom.
            items(messages.reversed()) { message ->
                MessageBubble(message = message)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        // Show the "GPT is thinking..." prompt if the API is processing.
        if (isGptThinking) {
            Text(
                text = "GPT is thinking...",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your message") }
            )
            Button(onClick = {
                if (inputText.isNotBlank()) {
                    onSendMessage(inputText)
                    inputText = ""
                    coroutineScope.launch {
                        // Scroll to the bottom (the first item in reverse layout)
                        scrollState.animateScrollToItem(0)
                    }
                }
            }) {
                Text("Send")
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    // Set different bubble colors for user and bot
    val bubbleColor = if (message.sender == "user") {
        Color(0xFFDCF8C6) // A light green for user messages
    } else {
        Color(0xFFEFEFEF) // A light gray for bot responses
    }
    // Align user messages on the right, bot messages on the left.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.sender == "user") Arrangement.End else Arrangement.Start
    ) {
        Card(
            backgroundColor = bubbleColor,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp),
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}
