package com.hse.impressionsplanner.ui.constructor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hse.impressionsplanner.data.Place
import com.hse.impressionsplanner.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val peopleFilterOptions = listOf("Все", "Один", "Пара", "Небольшая компания", "Большая компания")
val ageFilterOptions    = listOf("Дети (0-6)", "Школьники (7-12)", "Подростки (13-17)", "Взрослые (18+)", "Пенсионеры (65+)")
val typeFilterOptions   = listOf("Активный", "Спокойный", "Экстрим", "Культура", "Гастрономия", "Природа")

private val ageToCategories = mapOf(
    "Дети (0-6)"       to setOf("Парки", "Развлечения"),
    "Школьники (7-12)" to setOf("Парки", "Музеи", "Развлечения"),
    "Подростки (13-17)" to setOf("Парки", "Музеи", "Развлечения", "Исторические места", "Рестораны"),
    "Взрослые (18+)"   to setOf("Парки", "Музеи", "Рестораны", "Исторические места", "Развлечения"),
    "Пенсионеры (65+)" to setOf("Музеи", "Парки", "Исторические места")
)

private val typeToCategories = mapOf(
    "Активный"   to setOf("Развлечения", "Парки"),
    "Спокойный"  to setOf("Музеи", "Исторические места", "Рестораны", "Парки"),
    "Экстрим"    to setOf("Развлечения"),
    "Культура"   to setOf("Музеи", "Исторические места"),
    "Гастрономия" to setOf("Рестораны"),
    "Природа"    to setOf("Парки")
)

private val peopleToCategories = mapOf(
    "Пара"              to setOf("Рестораны", "Исторические места", "Парки", "Музеи"),
    "Небольшая компания" to setOf("Парки", "Музеи", "Развлечения", "Рестораны"),
    "Большая компания"  to setOf("Парки", "Развлечения")
)

class ConstructorViewModel : ViewModel() {

    private val repository = PlaceRepository()
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _allPlaces = MutableStateFlow<List<Place>>(emptyList())

    private val _searchQuery  = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Фильтры
    private val _peopleFilter = MutableStateFlow("Все")
    val peopleFilter: StateFlow<String> = _peopleFilter.asStateFlow()

    private val _ageFilters = MutableStateFlow<Set<String>>(emptySet())
    val ageFilters: StateFlow<Set<String>> = _ageFilters.asStateFlow()

    private val _typeFilters = MutableStateFlow<Set<String>>(emptySet())
    val typeFilters: StateFlow<Set<String>> = _typeFilters.asStateFlow()

    val activeFilterCount: StateFlow<Int> = combine(
        _peopleFilter, _ageFilters, _typeFilters
    ) { people, ages, types ->
        (if (people != "Все") 1 else 0) + ages.size + types.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val filteredPlaces: StateFlow<List<Place>> = combine(
        _allPlaces, _searchQuery, _peopleFilter, _ageFilters, _typeFilters
    ) { places, query, people, ages, types ->
        var result = places

        val allowedByPeople = peopleToCategories[people]
        if (allowedByPeople != null) {
            result = result.filter { it.category in allowedByPeople }
        }

        if (ages.isNotEmpty()) {
            val allowed = ages.flatMap { ageToCategories[it] ?: emptySet() }.toSet()
            result = result.filter { it.category in allowed }
        }

        if (types.isNotEmpty()) {
            val allowed = types.flatMap { typeToCategories[it] ?: emptySet() }.toSet()
            result = result.filter { it.category in allowed }
        }

        if (query.isNotBlank()) {
            result = result.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.address.contains(query, ignoreCase = true)
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _routePlaces = MutableStateFlow<List<Place>>(emptyList())
    val routePlaces: StateFlow<List<Place>> = _routePlaces.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Событие запуска Яндекс Карт
    private val _launchMapEvent = MutableSharedFlow<String>(replay = 0)
    val launchMapEvent: SharedFlow<String> = _launchMapEvent.asSharedFlow()

    // Событие "достигнут максимум 8 мест"
    private val _maxReachedEvent = MutableSharedFlow<Unit>(replay = 0)
    val maxReachedEvent: SharedFlow<Unit> = _maxReachedEvent.asSharedFlow()

    // Событие "требуется авторизация"
    private val _needsAuthEvent = MutableSharedFlow<Unit>(replay = 0)
    val needsAuthEvent: SharedFlow<Unit> = _needsAuthEvent.asSharedFlow()

    init { loadPlaces() }

    private fun loadPlaces() {
        viewModelScope.launch {
            _isLoading.value = true
            try { _allPlaces.value = repository.getPlaces() }
            finally { _isLoading.value = false }
        }
    }

    fun updateSearch(query: String) { _searchQuery.value = query }

    fun setPeopleFilter(v: String) { _peopleFilter.value = v }

    fun toggleAgeFilter(age: String) {
        val s = _ageFilters.value.toMutableSet()
        if (!s.add(age)) s.remove(age)
        _ageFilters.value = s
    }

    fun toggleTypeFilter(type: String) {
        val s = _typeFilters.value.toMutableSet()
        if (!s.add(type)) s.remove(type)
        _typeFilters.value = s
    }

    fun clearFilters() {
        _peopleFilter.value = "Все"
        _ageFilters.value   = emptySet()
        _typeFilters.value  = emptySet()
    }

    fun togglePlace(place: Place) {
        val current = _routePlaces.value.toMutableList()
        if (current.contains(place)) {
            current.remove(place)
        } else if (current.size < 8) {
            current.add(place)
        } else {
            viewModelScope.launch { _maxReachedEvent.emit(Unit) }
        }
        _routePlaces.value = current
    }

    fun isInRoute(place: Place) = _routePlaces.value.contains(place)
    fun isMaxReached()          = _routePlaces.value.size >= 8

    fun buildRoute() {
        val places = _routePlaces.value
        if (places.isEmpty()) return

        val points = places.filter { it.lat != 0.0 && it.lon != 0.0 }
            .joinToString("~") { "${it.lat},${it.lon}" }

        val yandexUri = if (points.isNotBlank())
            "yandexmaps://maps.yandex.ru/?rtext=$points&rtt=pd"
        else
            "yandexmaps://maps.yandex.ru/?text=${places.first().address}"

        val userId = auth.currentUser?.uid
        if (userId == null) {
            // Незарегистрированный пользователь → перенаправление на авторизацию
            viewModelScope.launch { _needsAuthEvent.emit(Unit) }
            return
        }

        viewModelScope.launch {
            try {
                val placeNames = places.joinToString(", ") { it.name }
                val now = System.currentTimeMillis()
                db.collection("saved_routes").add(
                    mapOf(
                        "userId"   to userId,
                        "placeIds" to places.map { it.id },
                        "date"     to now,
                        "type"     to "constructed"
                    )
                ).await()
                db.collection("diary_entries").add(
                    mapOf(
                        "userId"   to userId,
                        "date"     to now,
                        "type"     to "constructed",
                        "raw_text" to placeNames,
                        "place"    to places.first().name,
                        "summary"  to "Маршрут: $placeNames",
                        "duration" to "${places.size} точек"
                    )
                ).await()
            } catch (_: Exception) { }
            _launchMapEvent.emit(yandexUri)
        }
    }
}
