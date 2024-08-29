package com.example.firstmessageapp

data class Chat(val name: String,
                val lastMessage: String,
                val timestamp: String,
                val unreadCount: Int,
                val profileImageUrl: String?
    )