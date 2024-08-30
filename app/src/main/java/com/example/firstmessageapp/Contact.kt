package com.example.firstmessageapp

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val profileImageUrl: String? = null
)