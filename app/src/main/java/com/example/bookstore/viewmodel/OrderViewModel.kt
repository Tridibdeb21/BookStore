package com.example.bookstore.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.bookstore.model.Order
import com.example.bookstore.model.ReturnRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * ViewModel responsible for managing and fetching the user's order history and return requests.
 */
class OrderViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _userOrders = MutableStateFlow<List<Order>>(emptyList())
    val userOrders: StateFlow<List<Order>> = _userOrders.asStateFlow()

    private val _returnRequests = MutableStateFlow<List<ReturnRequest>>(emptyList())
    val returnRequests: StateFlow<List<ReturnRequest>> = _returnRequests.asStateFlow()

    private var ordersListener: ListenerRegistration? = null
    private var returnsListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                fetchUserOrders(user.uid)
                fetchReturnRequests(user.uid)
            } else {
                clearData()
            }
        }
    }

    fun clearData() {
        ordersListener?.remove()
        ordersListener = null
        returnsListener?.remove()
        returnsListener = null
        _userOrders.value = emptyList()
        _returnRequests.value = emptyList()
    }

    /**
     * Attempts to fetch the user's orders sorted by date descending from Firestore.
     * Note: This requires a composite index in Firestore. If the index is missing,
     * it falls back to a simpler query and sorts locally.
     * 
     * @param userId The ID of the authenticated user whose orders should be fetched.
     */
    private fun fetchUserOrders(userId: String) {
        ordersListener?.remove()
        ordersListener = db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("OrderViewModel", "Sorted query failed, falling back: ${error.message}")
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

    /**
     * Listens for return requests submitted by the current user.
     */
    private fun fetchReturnRequests(userId: String) {
        returnsListener?.remove()
        returnsListener = db.collection("returns")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("OrderViewModel", "Error fetching return requests: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _returnRequests.value = snapshot.documents
                        .mapNotNull { it.toObject(ReturnRequest::class.java)?.copy(id = it.id) }
                }
            }
    }

    /**
     * Submits a return/refund request for a specific book in an order.
     */
    fun submitReturnRequest(orderId: String, bookId: String, bookTitle: String, reason: String) {
        val userId = auth.currentUser?.uid ?: return
        val id = UUID.randomUUID().toString()
        val request = ReturnRequest(
            id = id,
            orderId = orderId,
            userId = userId,
            bookId = bookId,
            bookTitle = bookTitle,
            reason = reason,
            status = "pending",
            timestamp = System.currentTimeMillis()
        )
        db.collection("returns").document(id).set(request)
            .addOnFailureListener { e ->
                Log.e("OrderViewModel", "Failed to submit return request: ${e.message}")
            }
    }

    override fun onCleared() {
        super.onCleared()
        ordersListener?.remove()
        returnsListener?.remove()
    }
}
