package com.example.bookstore.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bookstore.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the current authentication state of a user.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * ViewModel responsible for user authentication flows, including login, registration, 
 * and session management via FirebaseAuth. It also fetches additional user role 
 * metadata from Firestore.
 */
class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        auth.currentUser?.let { user ->
            fetchUserRole(user.uid)
        }
    }

    /**
     * Attempts to log the user in using their email and password.
     * Updates [authState] based on the result (Success or Error).
     *
     * @param email The user's email address.
     * @param pass The user's password.
     */
    fun login(email: String, pass: String) {
        if(email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                fetchUserRole(it.user?.uid ?: "")
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
    }

    /**
     * Registers a new user with FirebaseAuth and creates a corresponding user document
     * in Firestore with a default "user" role.
     *
     * @param email The new user's email address.
     * @param pass The new user's desired password.
     */
    fun register(email: String, pass: String) {
        if(email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                val uid = it.user?.uid ?: ""
                val newUser = User(uid = uid, email = email, role = "user")
                db.collection("users").document(uid).set(newUser)
                    .addOnSuccessListener {
                        _authState.value = AuthState.Success(newUser)
                    }
                    .addOnFailureListener { e ->
                        _authState.value = AuthState.Error(e.message ?: "Failed to save user")
                    }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Registration failed")
            }
    }

    private fun fetchUserRole(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val role = doc.getString("role") ?: "user"
                    val email = doc.getString("email") ?: ""
                    _authState.value = AuthState.Success(User(uid = uid, email = email, role = role))
                } else {
                    _authState.value = AuthState.Error("User data not found")
                }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Failed to fetch user data")
            }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
