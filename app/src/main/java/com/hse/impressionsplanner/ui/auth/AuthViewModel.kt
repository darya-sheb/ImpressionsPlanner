package com.hse.impressionsplanner.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _currentUser.value = auth.currentUser
            } catch (e: Exception) {
                _error.value = parseError(e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _currentUser.value = auth.currentUser
            } catch (e: Exception) {
                _error.value = parseError(e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    fun clearError() {
        _error.value = null
    }

    private fun parseError(message: String?): String {
        return when {
            message?.contains("no user record") == true -> "Пользователь не найден"
            message?.contains("password is invalid") == true -> "Неверный пароль"
            message?.contains("badly formatted") == true -> "Некорректный email"
            message?.contains("email address is already") == true -> "Email уже используется"
            message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "Неверный email или пароль"
            else -> "Ошибка: $message"
        }
    }
}