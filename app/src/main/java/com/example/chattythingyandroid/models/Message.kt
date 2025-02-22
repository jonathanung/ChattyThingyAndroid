package com.example.chattythingyandroid.models

data class Message(
    val id: String = "",
    val text: String = "",
    val sender: String = "user", // "user" or "bot"
    val timestamp: Long = System.currentTimeMillis()
)
