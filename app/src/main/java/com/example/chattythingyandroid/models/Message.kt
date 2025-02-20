package com.example.chattythingyandroid.models

data class Message(
    val id: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
