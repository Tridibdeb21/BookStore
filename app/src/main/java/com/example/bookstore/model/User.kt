package com.example.bookstore.model

data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "user", // "user" or "admin"
    val wishlist: List<String> = emptyList(),
    val cart: Map<String, Int> = emptyMap() // bookId to quantity
)

