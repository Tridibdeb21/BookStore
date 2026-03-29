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

    init {
        fetchData()
    }

    private fun fetchData() {
        _isLoading.value = true
        // Fetch Categories
        db.collection("categories").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val catList = snapshot.documents.mapNotNull { it.toObject(Category::class.java)?.copy(id = it.id) }
                _categories.value = catList
            }
        }

        // Fetch Books
        db.collection("books").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val bookList = snapshot.documents.mapNotNull { it.toObject(Book::class.java)?.copy(id = it.id) }
                _books.value = bookList
                _isLoading.value = false
            }
        }
    }
}
