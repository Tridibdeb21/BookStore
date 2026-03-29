package com.example.bookstore.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bookstore.model.Book
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WishlistViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _wishlistBooks = MutableStateFlow<List<Book>>(emptyList())
    val wishlistBooks: StateFlow<List<Book>> = _wishlistBooks.asStateFlow()

    private val _wishlistIds = MutableStateFlow<List<String>>(emptyList())
    val wishlistIds: StateFlow<List<String>> = _wishlistIds.asStateFlow()

    init {
        listenToWishlist()
    }

    private fun listenToWishlist() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val ids = snapshot.get("wishlist") as? List<String> ?: emptyList()
                _wishlistIds.value = ids
                
                if (ids.isNotEmpty()) {
                    db.collection("books").whereIn("id", ids).get()
                        .addOnSuccessListener { booksSnapshot ->
                            val books = booksSnapshot.documents.mapNotNull { it.toObject(Book::class.java)?.copy(id = it.id) }
                            _wishlistBooks.value = books
                        }
                } else {
                    _wishlistBooks.value = emptyList()
                }
            }
        }
    }

    fun toggleWishlist(bookId: String) {
        val uid = auth.currentUser?.uid ?: return
        val isCurrentlyWishlisted = _wishlistIds.value.contains(bookId)
        
        val userRef = db.collection("users").document(uid)
        if (isCurrentlyWishlisted) {
            userRef.update("wishlist", FieldValue.arrayRemove(bookId))
        } else {
            userRef.update("wishlist", FieldValue.arrayUnion(bookId))
        }
    }
}
