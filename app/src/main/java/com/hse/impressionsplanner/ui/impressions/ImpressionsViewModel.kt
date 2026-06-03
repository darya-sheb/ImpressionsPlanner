package com.hse.impressionsplanner.ui.impressions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hse.impressionsplanner.data.Route
import com.hse.impressionsplanner.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImpressionsViewModel : ViewModel() {

    private val repository = PlaceRepository()

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes.asStateFlow()

    private val _savedRoutes = MutableStateFlow<Set<String>>(emptySet())
    val savedRoutes: StateFlow<Set<String>> = _savedRoutes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadRoutes()
    }

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
}