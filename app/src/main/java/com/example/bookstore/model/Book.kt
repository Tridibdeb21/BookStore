package com.example.bookstore.model

data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val categoryId: String = "",
    val description: String = "",
    val previewImages: List<String> = emptyList(), // Up to 5 image URLs
    val price: Double = 0.0,
    val availabilityStatus: String = "in_stock",
    val imageUrl: String = "", // main cover
    val pdfUrl: String = "", // preview PDF URL
    val rating: Double = 0.0,
    val reviewsCount: Int = 0
)
