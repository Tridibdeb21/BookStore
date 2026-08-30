package com.example.bookstore.model

data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "user", // "user" or "admin"
    val wishlist: List<String> = emptyList(),
    val cart: Map<String, Int> = emptyMap(), // bookId to quantity
    val pastReadingSpeeds: List<Int> = emptyList(), // User's past reading speeds in pages per hour
    val firstEditionBadges: List<String> = emptyList(), // bookIds for which the user got a first edition badge
    val yearlyGoal: Int = 0,
    val booksFinishedThisYear: Int = 0,
    val readingStreak: Int = 0,
    val lastActiveDate: Long = 0L
)

