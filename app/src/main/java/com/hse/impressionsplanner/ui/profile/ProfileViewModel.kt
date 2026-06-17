package com.hse.impressionsplanner.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val avatarEmojis = listOf(
    "🙂", "😎", "🤩", "🥳",
    "🐱", "🐶", "🦊", "🐻",
    "🏔️", "🌊", "🌅", "✨"
)

class ProfileViewModel : ViewModel() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _avatarEmoji = MutableStateFlow("🙂")
    val avatarEmoji: StateFlow<String> = _avatarEmoji.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _editingName = MutableStateFlow(false)
    val editingName: StateFlow<Boolean> = _editingName.asStateFlow()

    private val _nameInput = MutableStateFlow("")
    val nameInput: StateFlow<String> = _nameInput.asStateFlow()

    private val _showEmojiPicker = MutableStateFlow(false)
    val showEmojiPicker: StateFlow<Boolean> = _showEmojiPicker.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val doc = db.collection("users").document(uid).get().await()
                _displayName.value = doc.getString("name") ?: ""
                _avatarEmoji.value = doc.getString("avatarEmoji") ?: "🙂"
            } catch (_: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startEditName() {
        _nameInput.value   = _displayName.value
        _editingName.value = true
    }

    fun cancelEditName() { _editingName.value = false }

    fun updateNameInput(text: String) { _nameInput.value = text }

    fun saveName() {
        val uid  = auth.currentUser?.uid ?: return
        val name = _nameInput.value.trim()
        _displayName.value  = name
        _editingName.value  = false
        viewModelScope.launch {
            try {
                db.collection("users").document(uid)
                    .set(mapOf("name" to name), SetOptions.merge())
                    .await()
            } catch (_: Exception) {}
        }
    }

    fun openEmojiPicker()  { _showEmojiPicker.value = true  }
    fun closeEmojiPicker() { _showEmojiPicker.value = false }

    fun selectEmoji(emoji: String) {
        val uid = auth.currentUser?.uid ?: return
        _avatarEmoji.value      = emoji
        _showEmojiPicker.value  = false
        viewModelScope.launch {
            try {
                db.collection("users").document(uid)
                    .set(mapOf("avatarEmoji" to emoji), SetOptions.merge())
                    .await()
            } catch (_: Exception) { }
        }
    }
}
