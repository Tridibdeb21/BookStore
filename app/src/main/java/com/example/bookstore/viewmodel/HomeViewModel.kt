package com.example.bookstore.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bookstore.model.Book
import com.example.bookstore.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    private var booksListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var categoriesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    init {
        auth.addAuthStateListener {
            refreshData()
        }
    }

    fun refreshData() {
        clearData()
        fetchData()
    }

    fun clearData() {
        booksListener?.remove()
        categoriesListener?.remove()
        booksListener = null
        categoriesListener = null
        
        _books.value = emptyList()
        _categories.value = emptyList()
        _isLoading.value = true
    }

    private fun fetchData() {
        _isLoading.value = true
        
        // Fetch Categories
        categoriesListener = db.collection("categories").addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("HomeViewModel", "Error fetching categories: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val catList = snapshot.documents.mapNotNull { it.toObject(Category::class.java)?.copy(id = it.id) }
                _categories.value = catList
            }
        }

        // Fetch Books
        booksListener = db.collection("books").addSnapshotListener { snapshot, error ->
            _isLoading.value = false
            if (error != null) {
                android.util.Log.e("HomeViewModel", "Error fetching books: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val bookList = snapshot.documents.mapNotNull { it.toObject(Book::class.java)?.copy(id = it.id) }
                _books.value = bookList
            }
        }
    }
}
