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

class OrderViewModel : ViewModel() {
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
                _userOrders.value = emptyList()
            }
        }
    }

    private var ordersListener: ListenerRegistration? = null

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
