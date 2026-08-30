package com.example.bookstore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.model.ShelfItem
import com.example.bookstore.util.CryptoUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShelfViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _shelfItems = MutableStateFlow<List<ShelfItem>>(emptyList())
    val shelfItems: StateFlow<List<ShelfItem>> = _shelfItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    init {
        listenToShelf()
    }

    fun listenToShelf() {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true
        listenerRegistration?.remove()
        
        listenerRegistration = db.collection("users")
            .document(uid)
            .collection("shelf")
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null || snapshot == null) return@addSnapshotListener
                
                val items = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ShelfItem::class.java)?.copy(id = doc.id)
                }
                _shelfItems.value = items
            }
    }

    fun addToShelf(bookId: String, title: String, coverUrl: String, author: String, status: String = "To Read", note: String = "") {
        val uid = auth.currentUser?.uid ?: return
        val encryptedNote = if (note.isNotBlank()) CryptoUtils.encrypt(note, uid) else ""
        
        val shelfItem = ShelfItem(
            id = bookId,
            bookId = bookId,
            title = title,
            coverUrl = coverUrl,
            author = author,
            status = status,
            noteEncrypted = encryptedNote,
            addedAt = System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .collection("shelf")
            .document(bookId)
            .set(shelfItem)
            .addOnSuccessListener {
                if (status == "Finished") {
                    onBookFinished(uid)
                }
            }
    }

    fun updateShelfStatus(bookId: String, newStatus: String) {
        val uid = auth.currentUser?.uid ?: return
        
        db.collection("users")
            .document(uid)
            .collection("shelf")
            .document(bookId)
            .update("status", newStatus)
            .addOnSuccessListener {
                if (newStatus == "Finished") {
                    onBookFinished(uid)
                }
            }
    }

    private fun onBookFinished(uid: String) {
        val userRef = db.collection("users").document(uid)
        userRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val currentFinished = doc.getLong("booksFinishedThisYear")?.toInt() ?: 0
                val yearlyGoal = doc.getLong("yearlyGoal")?.toInt() ?: 0
                val newFinished = currentFinished + 1
                
                val badges = (doc.get("unlockedBadges") as? List<String>)?.toMutableList() ?: mutableListOf()
                
                if (newFinished >= 1 && !badges.contains("First Step")) {
                    badges.add("First Step")
                }
                if (yearlyGoal > 0) {
                    if (newFinished >= (yearlyGoal / 2) && !badges.contains("Halfway There")) {
                        badges.add("Halfway There")
                    }
                    if (newFinished >= yearlyGoal && !badges.contains("Goal Achiever")) {
                        badges.add("Goal Achiever")
                    }
                }

                userRef.update(
                    mapOf(
                        "booksFinishedThisYear" to newFinished,
                        "unlockedBadges" to badges
                    )
                )
            }
        }
    }

    fun decryptNote(noteEncrypted: String): String {
        val uid = auth.currentUser?.uid ?: return ""
        return CryptoUtils.decrypt(noteEncrypted, uid)
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
