package com.example.socialmediaapp.data

data class Post(
    val id: String,
    val userId: String,
    val content: String,
    val imageUrl: String?,
    val timestamp: Long
)
