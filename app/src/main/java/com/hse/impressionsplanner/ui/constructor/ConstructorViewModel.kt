package com.hse.impressionsplanner.ui.constructor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hse.impressionsplanner.data.Place
import com.hse.impressionsplanner.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConstructorViewModel : ViewModel() {
    private val repository = PlaceRepository()
    private val _allPlaces = MutableStateFlow<List<Place>>(emptyList())
    val allPlaces: StateFlow<List<Place>> = _allPlaces.asStateFlow()
    private val _selectedCategory = MutableStateFlow("Все")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    private val _routePlaces = MutableStateFlow<List<Place>>(emptyList())
    val routePlaces: StateFlow<List<Place>> = _routePlaces.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPlaces()
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allPlaces.value = repository.getPlaces()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getFiltered(): List<Place> {
        val cat = _selectedCategory.value
        return if (cat == "Все") _allPlaces.value
        else _allPlaces.value.filter { it.category == cat }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun togglePlace(place: Place) {
        val current = _routePlaces.value.toMutableList()
        if (current.contains(place)) current.remove(place) else current.add(place)
        _routePlaces.value = current
    }

    fun isInRoute(place: Place) = _routePlaces.value.contains(place)
}