package com.example.bookstore.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bookstore.model.Book
import com.example.bookstore.model.Coupon
import com.example.bookstore.model.Order
import com.example.bookstore.model.OrderItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.tasks.await

data class CartItem(val book: Book, val quantity: Int = 1)

class CartViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _checkoutStatus = MutableStateFlow<String?>(null)
    val checkoutStatus: StateFlow<String?> = _checkoutStatus.asStateFlow()

    private val _lastOrder = MutableStateFlow<Order?>(null)
    val lastOrder: StateFlow<Order?> = _lastOrder.asStateFlow()

    fun resetLastOrder() { _lastOrder.value = null }

    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    private val _couponError = MutableStateFlow<String?>(null)
    val couponError: StateFlow<String?> = _couponError.asStateFlow()

    private val _selectedBookIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedBookIds: StateFlow<Set<String>> = _selectedBookIds.asStateFlow()

    fun toggleSelection(bookId: String) {
        val current = _selectedBookIds.value.toMutableSet()
        if (current.contains(bookId)) current.remove(bookId) else current.add(bookId)
        _selectedBookIds.value = current
    }

    fun selectAll() {
        _selectedBookIds.value = _cartItems.value.map { it.book.id }.toSet()
    }

    fun clearSelection() {
        _selectedBookIds.value = emptySet()
    }

    private var cartListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                listenToCart()
            } else {
                clearData()
            }
        }
    }

    fun clearData() {
        cartListener?.remove()
        cartListener = null
        _cartItems.value = emptyList()
        _selectedBookIds.value = emptySet()
        _appliedCoupon.value = null
        _couponError.value = null
    }

    fun listenToCart() {
        cartListener?.remove()
        val uid = auth.currentUser?.uid ?: return
        cartListener = db.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            
            try {
                // Firestore numbers are Long by default, so we cast to Map<String, Any> and convert values
                val rawCart = snapshot.get("cart") as? Map<String, Any> ?: emptyMap()
                if (rawCart.isEmpty()) {
                    _cartItems.value = emptyList()
                    return@addSnapshotListener
                }
                
                val cartMap = rawCart.mapValues { (_, value) -> (value as? Number)?.toInt() ?: 1 }
                val bookIds = cartMap.keys.toList()
                
                // Firestore whereIn limit is 10. For simplicity, we take first 10 for now or chunk it.
                // In a premium app, we should chunk, but let's at least prevent the crash first.
                val safeIds = bookIds.take(10)
                
                db.collection("books").whereIn("id", safeIds).get()
                    .addOnSuccessListener { booksSnapshot ->
                        val books = booksSnapshot.documents.mapNotNull { it.toObject(Book::class.java)?.copy(id = it.id) }
                        val items = books.map { book ->
                            CartItem(book, cartMap[book.id] ?: 1)
                        }
                        _cartItems.value = items
                        // Auto-select all items when cart is first loaded
                        if (_selectedBookIds.value.isEmpty()) {
                            _selectedBookIds.value = items.map { it.book.id }.toSet()
                        }
                    }
                    .addOnFailureListener {
                        // Handle failure gracefully
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateFirestoreCart(newCartMap: Map<String, Int>) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update("cart", newCartMap)
    }

    // Reactive subtotal and total (based on selected items only)
    val subtotal: StateFlow<Double> = combine(_cartItems, _selectedBookIds) { items, selected ->
        items.filter { it.book.id in selected }.sumOf { it.book.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val total: StateFlow<Double> = combine(_cartItems, _appliedCoupon, _selectedBookIds) { items, coupon, selected ->
        val sub = items.filter { it.book.id in selected }.sumOf { it.book.price * it.quantity }
        val discount = coupon?.discountPercent ?: 0
        sub * (1 - (discount / 100.0))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(book: Book, quantity: Int = 1) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
            val rawCart = snapshot.get("cart") as? Map<String, Any> ?: emptyMap()
            val newCartMap = rawCart.mapValues { (_, v) -> (v as? Number)?.toInt() ?: 1 }.toMutableMap()
            newCartMap[book.id] = (newCartMap[book.id] ?: 0) + quantity
            updateFirestoreCart(newCartMap)
        }
    }

    fun removeFromCart(bookId: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
            val rawCart = snapshot.get("cart") as? Map<String, Any> ?: emptyMap()
            val newCartMap = rawCart.mapValues { (_, v) -> (v as? Number)?.toInt() ?: 1 }.toMutableMap()
            newCartMap.remove(bookId)
            updateFirestoreCart(newCartMap)
        }
    }

    fun decreaseQuantity(bookId: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
            val rawCart = snapshot.get("cart") as? Map<String, Any> ?: emptyMap()
            val newCartMap = rawCart.mapValues { (_, v) -> (v as? Number)?.toInt() ?: 1 }.toMutableMap()
            val currentQty = newCartMap[bookId] ?: 1
            if (currentQty > 1) {
                newCartMap[bookId] = currentQty - 1
                updateFirestoreCart(newCartMap)
            } else {
                removeFromCart(bookId)
            }
        }
    }

    fun clearCart() {
        updateFirestoreCart(emptyMap())
        _appliedCoupon.value = null
        _couponError.value = null
    }


    fun applyCoupon(code: String) {
        if (code.isBlank()) return
        _couponError.value = null
        viewModelScope.launch {
            try {
                val snapshot = db.collection("coupons").whereEqualTo("code", code.uppercase()).get().await()
                if (!snapshot.isEmpty) {
                    val coupon = snapshot.documents.first().toObject(Coupon::class.java)
                    if (coupon != null && coupon.isActive) {
                        _appliedCoupon.value = coupon
                        _couponError.value = null
                    } else {
                        _couponError.value = "Coupon is inactive"
                    }
                } else {
                    _couponError.value = "Invalid coupon code"
                }
            } catch (e: Exception) {
                _couponError.value = "Error validating coupon: ${e.message}"
            }
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _couponError.value = null
    }
    
    fun checkout() {
        val uid = auth.currentUser?.uid
        val email = auth.currentUser?.email
        if (uid == null) {
            _checkoutStatus.value = "Error: User not logged in"
            return
        }
        val items = _cartItems.value
        val selectedItems = items.filter { it.book.id in _selectedBookIds.value }
        if (selectedItems.isEmpty()) {
            _checkoutStatus.value = "Error: Please select at least one item"
            return
        }
        
        _checkoutStatus.value = "Processing..."

        val orderItems = selectedItems.map {
            OrderItem(it.book.id, it.book.title, it.quantity, it.book.price)
        }
        val currentTotal = total.value
        
        val newOrderRef = db.collection("orders").document()
        val order = Order(
            id = newOrderRef.id,
            userId = uid,
            userEmail = email ?: "",
            items = orderItems,
            totalAmount = currentTotal,
            status = "Pending",
            date = System.currentTimeMillis()
        )

        db.runTransaction { transaction ->
            // Step 1: Read all current stock levels
            val bookRefs = selectedItems.associate { it.book.id to db.collection("books").document(it.book.id) }
            val bookSnapshots = bookRefs.mapValues { entry -> 
                val snap = transaction.get(entry.value)
                if (!snap.exists()) throw Exception("Book not found: ${entry.key}")
                snap
            }

            // Step 2: Validate stock
            for (item in selectedItems) {
                val snapshot = bookSnapshots[item.book.id]!!
                val currentStock = snapshot.getLong("stockQuantity")?.toInt() ?: 0
                if (currentStock < item.quantity) {
                    throw Exception("Not enough stock for '${item.book.title}'. Only $currentStock available.")
                }
            }

            // Step 3: Deduct stock and update availability
            for (item in selectedItems) {
                val snapshot = bookSnapshots[item.book.id]!!
                val currentStock = snapshot.getLong("stockQuantity")?.toInt() ?: 0
                val newStock = currentStock - item.quantity
                val availabilityStatus = if (newStock > 0) "in_stock" else "out_of_stock"
                transaction.update(bookRefs[item.book.id]!!, mapOf(
                    "stockQuantity" to newStock,
                    "availabilityStatus" to availabilityStatus
                ))
            }

            // Step 4: Save the new order
            transaction.set(newOrderRef, order)
        }
        .addOnSuccessListener {
            _checkoutStatus.value = null
            _lastOrder.value = order
            // Remove the selected items from the cart upon successful checkout
            val uid2 = auth.currentUser?.uid ?: return@addOnSuccessListener
            
            db.collection("users").document(uid2).get().addOnSuccessListener { snapshot ->
                val rawCart = snapshot.get("cart") as? Map<String, Any> ?: emptyMap()
                val newCartMap = rawCart.mapValues { (_, v) -> (v as? Number)?.toInt() ?: 1 }.toMutableMap()
                
                selectedItems.forEach {
                    newCartMap.remove(it.book.id)
                }
                db.collection("users").document(uid2).update("cart", newCartMap)
                clearSelection() // Clear the local selection state
            }
        }
        .addOnFailureListener {
            _checkoutStatus.value = "Error: ${it.message}"
        }
    }
    
    fun resetCheckoutStatus() {
        _checkoutStatus.value = null
    }
}
