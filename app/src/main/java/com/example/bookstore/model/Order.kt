package com.example.bookstore.model

data class OrderItem(
    val bookId: String = "",
    val bookTitle: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0
)

data class Order(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "Pending",
    val date: Long = System.currentTimeMillis()
)
