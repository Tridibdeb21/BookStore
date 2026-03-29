package com.example.bookstore.model

data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
