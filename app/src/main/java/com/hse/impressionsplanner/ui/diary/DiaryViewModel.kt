package com.hse.impressionsplanner.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hse.impressionsplanner.data.DiaryEntry
import com.hse.impressionsplanner.data.repository.GigaChatRepository
import com.hse.impressionsplanner.data.repository.StructuredEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DiaryViewModel : ViewModel() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gigaChat = GigaChatRepository()

    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries: StateFlow<List<DiaryEntry>> = _entries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Состояние обработки GigaChat
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Структурированная запись для превью (null = превью не показывается)
    private val _previewEntry = MutableStateFlow<StructuredEntry?>(null)
    val previewEntry: StateFlow<StructuredEntry?> = _previewEntry.asStateFlow()

    // Сообщение об ошибке GigaChat
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init { loadEntries() }

    fun loadEntries() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("diary_entries")
                    .whereEqualTo("userId", userId)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .await()
                _entries.value = snapshot.documents.mapNotNull { doc ->
                    DiaryEntry(
                        id       = doc.id,
                        userId   = userId,
                        date     = doc.getLong("date") ?: System.currentTimeMillis(),
                        rawText  = doc.getString("raw_text") ?: "",
                        place    = doc.getString("place") ?: "",
                        duration = doc.getString("duration") ?: "",
                        company  = doc.getString("company") ?: "",
                        weather  = doc.getString("weather") ?: "",
                        food     = doc.getString("food") ?: "",
                        surprise = doc.getString("surprise") ?: "",
                        emotions = doc.getString("emotions") ?: "",
                        summary  = doc.getString("summary") ?: ""
                    )
                }
            } catch (_: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun openDialog()  { _showDialog.value = true }
    fun closeDialog() {
        _showDialog.value = false
        _inputText.value  = ""
        _previewEntry.value = null
        _errorMessage.value = null
    }

    fun updateInput(text: String) { _inputText.value = text }

    fun clearError() { _errorMessage.value = null }

    fun discardPreview() { _previewEntry.value = null }

    /** Отправляет текст в GigaChat и показывает превью структурированной записи */
    fun processWithGigaChat() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null
            try {
                val structured = gigaChat.structureEntry(text)
                _previewEntry.value = structured
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка GigaChat: ${e.message ?: "нет сети"}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /** Сохраняет структурированную запись из превью в Firestore */
    fun confirmPreview() {
        val userId  = auth.currentUser?.uid ?: return
        val preview = _previewEntry.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = mapOf(
                    "userId"   to userId,
                    "date"     to System.currentTimeMillis(),
                    "type"     to "impression",
                    "raw_text" to preview.rawText,
                    "place"    to preview.place,
                    "duration" to preview.duration,
                    "company"  to preview.company,
                    "weather"  to preview.weather,
                    "food"     to preview.food,
                    "surprise" to preview.surprise,
                    "emotions" to preview.emotions,
                    "summary"  to preview.summary
                )
                db.collection("diary_entries").add(data).await()
                closeDialog()
                loadEntries()
            } catch (e: Exception) {
                val newEntry = DiaryEntry(
                    id       = System.currentTimeMillis().toString(),
                    userId   = userId,
                    date     = System.currentTimeMillis(),
                    rawText  = preview.rawText,
                    place    = preview.place,
                    duration = preview.duration,
                    company  = preview.company,
                    weather  = preview.weather,
                    food     = preview.food,
                    surprise = preview.surprise,
                    emotions = preview.emotions,
                    summary  = preview.summary
                )
                _entries.value = listOf(newEntry) + _entries.value
                closeDialog()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Сохраняет текст напрямую без GigaChat */
    fun saveEntryRaw() {
        val userId = auth.currentUser?.uid ?: return
        val text   = _inputText.value.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = mapOf(
                    "userId"   to userId,
                    "date"     to System.currentTimeMillis(),
                    "type"     to "impression",
                    "raw_text" to text,
                    "place"    to "",
                    "summary"  to text
                )
                db.collection("diary_entries").add(data).await()
                closeDialog()
                loadEntries()
            } catch (e: Exception) {
                val newEntry = DiaryEntry(
                    id      = System.currentTimeMillis().toString(),
                    userId  = userId,
                    date    = System.currentTimeMillis(),
                    rawText = text,
                    summary = text
                )
                _entries.value = listOf(newEntry) + _entries.value
                closeDialog()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            try {
                db.collection("diary_entries").document(entryId).delete().await()
            } catch (_: Exception) { }
            _entries.value = _entries.value.filter { it.id != entryId }
        }
    }
}
