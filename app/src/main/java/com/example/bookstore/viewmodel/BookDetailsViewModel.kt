package com.example.bookstore.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bookstore.model.Book
import com.example.bookstore.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BookDetailsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadBook(bookId: String) {
        _isLoading.value = true
        db.collection("books").document(bookId)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    _book.value = snapshot.toObject(Book::class.java)?.copy(id = snapshot.id)
                }
                _isLoading.value = false
            }
        loadReviews(bookId)
    }

    private fun loadReviews(bookId: String) {
        db.collection("books").document(bookId).collection("reviews")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _reviews.value = snapshot.toObjects(Review::class.java)
                }
            }
    }

    fun submitReview(bookId: String, review: Review) {
        val bookRef = db.collection("books").document(bookId)
        val reviewRef = bookRef.collection("reviews").document(review.userId)

        db.runTransaction { transaction ->
            val bookSnapshot = transaction.get(bookRef)
            val existingReviewSnapshot = transaction.get(reviewRef)

            val currentBook = bookSnapshot.toObject(Book::class.java) ?: return@runTransaction
            
            // Handle missing fields in old documents
            var currentRating = bookSnapshot.getDouble("rating") ?: 0.0
            var currentCount = bookSnapshot.getLong("reviewsCount")?.toInt() ?: 0
            
            var newReviewsCount = currentCount
            var totalRatingSum = currentRating * currentCount

            if (existingReviewSnapshot.exists()) {
                val oldReviewRating = existingReviewSnapshot.getDouble("rating") ?: 0.0
                totalRatingSum = totalRatingSum - oldReviewRating + review.rating
            } else {
                newReviewsCount += 1
                totalRatingSum += review.rating
            }

            val newAvgRating = if (newReviewsCount > 0) totalRatingSum / newReviewsCount else 0.0

            transaction.set(reviewRef, review.copy(id = review.userId))
            transaction.update(bookRef, mapOf(
                "rating" to newAvgRating,
                "reviewsCount" to newReviewsCount
            ))
        }.addOnSuccessListener {
            android.util.Log.d("BookDetailsViewModel", "Review submitted successfully")
            loadBook(bookId) // Refresh local book state
        }.addOnFailureListener { e ->
            android.util.Log.e("BookDetailsViewModel", "Failed to submit review", e)
        }
    }
}
