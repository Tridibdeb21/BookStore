package com.example.bookstore.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bookstore.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrdersViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchOrders()
    }

    private fun fetchOrders() {
        val uid = auth.currentUser?.uid ?: return
        
        db.collection("orders")
            .whereEqualTo("userId", uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val orderList = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
                    _orders.value = orderList
                }
                _isLoading.value = false
            }
    }
}
