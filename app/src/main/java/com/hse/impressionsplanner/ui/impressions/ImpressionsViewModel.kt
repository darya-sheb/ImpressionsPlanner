package com.hse.impressionsplanner.ui.impressions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hse.impressionsplanner.data.Route
import com.hse.impressionsplanner.data.repository.PlaceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ImpressionsViewModel : ViewModel() {

    private val repository = PlaceRepository()
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes.asStateFlow()

    private val _savedRoutes = MutableStateFlow<Set<String>>(emptySet())
    val savedRoutes: StateFlow<Set<String>> = _savedRoutes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Флаг «сохранено в дневник»
    private val _savedToDiary = MutableStateFlow(false)
    val savedToDiary: StateFlow<Boolean> = _savedToDiary.asStateFlow()

    // Событие «требуется авторизация»
    private val _needsAuthEvent = MutableSharedFlow<Unit>(replay = 0)
    val needsAuthEvent: SharedFlow<Unit> = _needsAuthEvent.asSharedFlow()

    init { loadRoutes() }

    private fun loadRoutes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _routes.value = repository.getRoutes()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSave(routeId: String) {
        val current = _savedRoutes.value.toMutableSet()
        if (current.contains(routeId)) current.remove(routeId) else current.add(routeId)
        _savedRoutes.value = current
    }

    fun isSaved(routeId: String) = _savedRoutes.value.contains(routeId)

    fun saveRouteToDiary(route: Route) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            viewModelScope.launch { _needsAuthEvent.emit(Unit) }
            return
        }
        viewModelScope.launch {
            try {
                db.collection("diary_entries").add(
                    mapOf(
                        "userId"   to userId,
                        "date"     to System.currentTimeMillis(),
                        "type"     to "ready",
                        "raw_text" to route.description,
                        "place"    to route.name,
                        "summary"  to route.description
                    )
                ).await()
            } catch (e: Exception) { }

            _savedToDiary.value = true
            delay(2_000)
            _savedToDiary.value = false
        }
    }
}
