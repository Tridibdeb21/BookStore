package com.example.bookstore.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.bookstore.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel responsible for managing and fetching the user's order history.
 * It listens to order updates from Firestore in real-time, handling fallback
 * scenarios if database indexing isn't fully set up.
 */
class OrderViewModel : ViewModel() {
    // Firebase instances for database operations and authentication
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _userOrders = MutableStateFlow<List<Order>>(emptyList())
    val userOrders: StateFlow<List<Order>> = _userOrders.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                fetchUserOrders(user.uid)
            } else {
                clearData()
            }
        }
    }

    fun clearData() {
        ordersListener?.remove()
        ordersListener = null
        _userOrders.value = emptyList()
    }

    private var ordersListener: ListenerRegistration? = null

    /**
     * Attempts to fetch the user's orders sorted by date descending from Firestore.
     * Note: This requires a composite index in Firestore. If the index is missing,
     * it falls back to a simpler query and sorts locally.
     * 
     * @param userId The ID of the authenticated user whose orders should be fetched.
     */
    private fun fetchUserOrders(userId: String) {
        ordersListener?.remove()
        // Try the sorted query first (requires a Firestore composite index)
        ordersListener = db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("OrderViewModel", "Sorted query failed, falling back: ${error.message}")
                    // Fallback: fetch without orderBy and sort locally
                    fetchUserOrdersFallback(userId)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _userOrders.value = snapshot.documents
                        .mapNotNull { it.toObject(Order::class.java)?.copy(id = it.id) }
                }
            }
    }

    /**
     * Fallback method used when the sorted query fails (e.g., due to missing composite index).
     * It fetches the orders directly and then sorts them by date locally in the app.
     * 
     * @param userId The ID of the authenticated user whose orders should be fetched.
     */
    private fun fetchUserOrdersFallback(userId: String) {
        ordersListener?.remove()
        ordersListener = db.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("OrderViewModel", "Fallback query also failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _userOrders.value = snapshot.documents
                        .mapNotNull { it.toObject(Order::class.java)?.copy(id = it.id) }
                        .sortedByDescending { it.date }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        ordersListener?.remove()
    }
}
