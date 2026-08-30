package com.example.bookstore.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.bookstore.model.UsedListing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * ViewModel to manage peer-to-peer used book listings.
 */
class UsedListingViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _listings = MutableStateFlow<List<UsedListing>>(emptyList())
    val listings: StateFlow<List<UsedListing>> = _listings.asStateFlow()

    private var listingsListener: ListenerRegistration? = null

    /**
     * Fetches active used listings for a specific book.
     */
    fun fetchListingsForBook(bookId: String) {
        listingsListener?.remove()
        listingsListener = db.collection("used_listings")
            .whereEqualTo("bookId", bookId)
            .whereEqualTo("status", "available")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UsedListingViewModel", "Error fetching listings: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _listings.value = snapshot.documents
                        .mapNotNull { it.toObject(UsedListing::class.java)?.copy(id = it.id) }
                        .sortedByDescending { it.timestamp }
                }
            }
    }

    /**
     * Submits a new used book listing to Firestore.
     */
    fun createListing(
        bookId: String,
        bookTitle: String,
        bookCoverUrl: String,
        askingPrice: Double,
        condition: String,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("User not authenticated.")
            return
        }
        val id = UUID.randomUUID().toString()
        val listing = UsedListing(
            id = id,
            bookId = bookId,
            bookTitle = bookTitle,
            bookCoverUrl = bookCoverUrl,
            askingPrice = askingPrice,
            condition = condition,
            description = description,
            sellerEmail = user.email ?: "Anonymous",
            sellerId = user.uid,
            status = "available",
            timestamp = System.currentTimeMillis()
        )
        db.collection("used_listings").document(id).set(listing)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to list the book.")
            }
    }

    override fun onCleared() {
        super.onCleared()
        listingsListener?.remove()
    }
}
