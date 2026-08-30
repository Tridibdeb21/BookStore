package com.example.bookstore.model

/**
 * Model representing a peer-to-peer used book listing in the marketplace.
 * Stored in Firestore under the "used_listings" collection.
 */
data class UsedListing(
    val id: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val bookCoverUrl: String = "",
    val askingPrice: Double = 0.0,
    val condition: String = "Good", // "Like New" | "Good" | "Acceptable"
    val description: String = "",
    val sellerEmail: String = "",
    val sellerId: String = "",
    val status: String = "available", // "available" | "sold"
    val timestamp: Long = System.currentTimeMillis()
)
