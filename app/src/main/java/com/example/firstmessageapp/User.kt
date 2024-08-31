package com.example.firstmessageapp

data class User(
    val userId: String = "",
    val name: String = "",
    val profileImageUrl: String = "",
    val isOnline: Boolean = false, // Indicates if the user is currently online
    val lastSeen: String? = null,  // Last seen timestamp
    val timestamp: String? = null
)