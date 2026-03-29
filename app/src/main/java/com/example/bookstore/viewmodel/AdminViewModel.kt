package com.example.bookstore.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.model.Book
import com.example.bookstore.model.Category
import com.example.bookstore.model.Coupon
import com.example.bookstore.model.Order
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _addBookStatus = MutableStateFlow<String?>(null)
    val addBookStatus: StateFlow<String?> = _addBookStatus.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    val allOrders: StateFlow<List<Order>> = _allOrders.asStateFlow()

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    init {
        fetchCategories()
        fetchBooks()
        fetchAllOrders()
        fetchCoupons()
    }

    private fun fetchCategories() {
        db.collection("categories").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _categories.value = snapshot.documents.mapNotNull { it.toObject(Category::class.java)?.copy(id = it.id) }
            }
        }
    }

    private fun fetchBooks() {
        db.collection("books").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _books.value = snapshot.documents.mapNotNull { it.toObject(Book::class.java)?.copy(id = it.id) }
            }
        }
    }

    private fun fetchAllOrders() {
        db.collection("orders").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _allOrders.value = snapshot.documents.mapNotNull { it.toObject(Order::class.java)?.copy(id = it.id) }
            }
        }
    }

    private fun fetchCoupons() {
        db.collection("coupons").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _coupons.value = snapshot.documents.mapNotNull { it.toObject(Coupon::class.java)?.copy(id = it.id) }
            }
        }
    }

    private val _orderUpdateStatus = MutableStateFlow<String?>(null)
    val orderUpdateStatus: StateFlow<String?> = _orderUpdateStatus.asStateFlow()

    fun updateOrderStatus(orderId: String, newStatus: String) {
        _orderUpdateStatus.value = "Updating..."
        db.collection("orders").document(orderId).update("status", newStatus)
            .addOnSuccessListener { _orderUpdateStatus.value = "Status updated successfully!" }
            .addOnFailureListener { _orderUpdateStatus.value = "Error: ${it.message}" }
    }

    fun clearOrderUpdateStatus() {
        _orderUpdateStatus.value = null
    }

    fun clearAddBookStatus() {
        _addBookStatus.value = null
    }

    fun addCategory(name: String, imageUrl: String) {
        val id = UUID.randomUUID().toString()
        val cat = Category(id, name, imageUrl)
        db.collection("categories").document(id).set(cat)
    }

    fun deleteCategory(id: String) {
        db.collection("categories").document(id).delete()
    }

    fun deleteBook(id: String) {
        db.collection("books").document(id).delete()
    }

    fun addCoupon(code: String, discountPercent: Int) {
        val id = UUID.randomUUID().toString()
        val coupon = Coupon(id, code, discountPercent)
        db.collection("coupons").document(id).set(coupon)
    }

    fun deleteCoupon(id: String) {
        db.collection("coupons").document(id).delete()
    }

    fun saveBook(bookId: String?, title: String, author: String, price: String, description: String, 
                 coverUrl: String, previewUrls: List<String>, pdfUrl: String, categoryId: String) {
        if(title.isBlank() || price.isBlank()) {
            _addBookStatus.value = "Title and Price are required"
            return
        }
        val priceDouble = price.toDoubleOrNull() ?: 0.0
        val targetId = if (bookId.isNullOrBlank()) UUID.randomUUID().toString() else bookId
        
        _addBookStatus.value = "Saving book... Please wait."
        
        viewModelScope.launch {
            try {
                _addBookStatus.value = "Saving book details..."
                
                val book = Book(
                    id = targetId,
                    title = title,
                    author = author,
                    categoryId = categoryId,
                    description = description,
                    previewImages = previewUrls,
                    price = priceDouble,
                    availabilityStatus = "in_stock",
                    imageUrl = coverUrl,
                    pdfUrl = pdfUrl
                )
                
                db.collection("books").document(targetId).set(book).await()
                _addBookStatus.value = "Success: Book saved!"
            } catch (e: Exception) {
                _addBookStatus.value = "Error: ${e.message}"
            }
        }
    }
}
