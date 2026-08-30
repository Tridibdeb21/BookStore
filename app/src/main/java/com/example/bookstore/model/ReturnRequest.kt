package com.example.bookstore.model

/**
 * Represents a return/refund request submitted by a user for a specific item in a delivered order.
 * Stored in the Firestore "returns" collection.
 */
data class ReturnRequest(
    val id: String = "",
    val orderId: String = "",
    val userId: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val reason: String = "",
    val status: String = "pending",   // "pending" | "approved" | "rejected"
    val timestamp: Long = System.currentTimeMillis()
)
