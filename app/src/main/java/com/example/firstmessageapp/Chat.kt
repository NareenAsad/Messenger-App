package com.example.firstmessageapp

data class Chat(
    val name: String? = null,
    val lastMessage: String? = null, // Correct field name
    val timestamp: String? = null, // Correct field name
    val unreadCount: Int = 0,
    val profileImageUrl: String? = null,
    val userId: String? = null
)