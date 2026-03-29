package com.example.bookstore.model

data class Coupon(
    val id: String = "",
    val code: String = "",
    val discountPercent: Int = 0,
    val isActive: Boolean = true,
    val expiryDate: Long = 0L
)
