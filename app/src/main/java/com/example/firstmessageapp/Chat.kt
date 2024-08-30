package com.example.firstmessageapp

data class Chat(
    val name: String? = null,
    val message: String? = null,
    val time: String? = null,
    val unreadCount: Int = 0,
    val profileImageUrl: String? = null,
    val userId: String? = null
)