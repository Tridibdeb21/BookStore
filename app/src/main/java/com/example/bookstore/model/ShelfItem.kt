package com.example.bookstore.model

data class ShelfItem(
    val id: String = "",
    val bookId: String = "",
    val title: String = "",
    val coverUrl: String = "",
    val author: String = "",
    val status: String = "To Read", // "To Read", "Reading", "Finished"
    val noteEncrypted: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
