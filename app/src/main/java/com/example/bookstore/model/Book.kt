package com.example.bookstore.model

/**
 * Data class representing a Book entity in the application.
 * This model maps directly to the documents stored in the Firestore "books" collection.
 */
data class Book(
    val id: String = "", // Unique identifier for the book, mapped from Firestore document ID
    val title: String = "", // The title of the book
    val author: String = "", // The name of the author
    val categoryId: String = "", // Reference to the category this book belongs to
    val description: String = "", // Synopsis or detailed description of the book's content
    val previewImages: List<String> = emptyList(), // Optional list of image URLs (up to 5) for book preview pages
    val price: Double = 0.0, // Selling price
    val availabilityStatus: String = "in_stock", // Inventory status (e.g., "in_stock", "out_of_stock")
    val imageUrl: String = "", // URL for the main cover image
    val pdfUrl: String = "", // URL for a preview PDF file to read a sample
    val rating: Double = 0.0, // Average community rating score (typically 1.0 to 5.0)
    val reviewsCount: Int = 0, // Total number of reviews submitted by users for this book
    val stockQuantity: Int = 0 // Number of physical items available in inventory
)
