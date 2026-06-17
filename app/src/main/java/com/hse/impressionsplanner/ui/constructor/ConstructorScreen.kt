package com.hse.impressionsplanner.ui.constructor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.hse.impressionsplanner.data.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstructorScreen(vm: ConstructorViewModel = viewModel()) {
    val searchQuery       by vm.searchQuery.collectAsState()
    val routePlaces       by vm.routePlaces.collectAsState()
    val isLoading         by vm.isLoading.collectAsState()
    val places            by vm.filteredPlaces.collectAsState()
    val activeFilterCount by vm.activeFilterCount.collectAsState()
    val peopleFilter      by vm.peopleFilter.collectAsState()
    val ageFilters        by vm.ageFilters.collectAsState()
    val typeFilters       by vm.typeFilters.collectAsState()

    val context           = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilters       by remember { mutableStateOf(false) }

    // Запуск Яндекс Карт
    LaunchedEffect(Unit) {
        vm.launchMapEvent.collect { uri ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            val canOpen = context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
            if (canOpen) {
                context.startActivity(intent)
            } else {
                val webUri = uri.replace("yandexmaps://maps.yandex.ru/", "https://yandex.ru/maps/")
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUri)))
            }
        }
    }

    // Snackbar при попытке добавить 9-ю точку
    LaunchedEffect(Unit) {
        vm.maxReachedEvent.collect {
            snackbarHostState.showSnackbar("Максимум 8 мест в маршруте")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (routePlaces.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick          = { vm.buildRoute() },
                    icon             = { Icon(Icons.Default.Map, contentDescription = null) },
                    text             = { Text("Создать маршрут (${routePlaces.size}/8)") },
                    containerColor   = MaterialTheme.colorScheme.primary,
                    contentColor     = MaterialTheme.colorScheme.onPrimary,
                    elevation        = FloatingActionButtonDefaults.elevation(8.dp),
                    modifier         = Modifier.padding(bottom = 8.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text       = "Конструктор маршрутов",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Поиск + кнопка Фильтры
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { vm.updateSearch(it) },
                    placeholder   = { Text("Поиск по названию или адресу") },
                    leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    modifier      = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BadgedBox(
                    badge = {
                        if (activeFilterCount > 0) {
                            Badge { Text("$activeFilterCount") }
                        }
                    }
                ) {
                    FilledTonalIconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтры")
                    }
                }
            }

            // Список мест
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (places.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text  = "Ничего не найдено",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(
                        start  = 16.dp, end = 16.dp, top = 4.dp,
                        bottom = if (routePlaces.isNotEmpty()) 96.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(places, key = { it.id }) { place ->
                        PlaceCard(
                            place      = place,
                            inRoute    = vm.isInRoute(place),
                            maxReached = vm.isMaxReached(),
                            onToggle   = { vm.togglePlace(place) }
                        )
                    }
                }
            }
        }
    }

    // Нижний лист фильтров
    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            FilterSheet(
                peopleFilter  = peopleFilter,
                ageFilters    = ageFilters,
                typeFilters   = typeFilters,
                onPeople      = { vm.setPeopleFilter(it) },
                onAgeToggle   = { vm.toggleAgeFilter(it) },
                onTypeToggle  = { vm.toggleTypeFilter(it) },
                onClear       = { vm.clearFilters() },
                onApply       = { showFilters = false }
            )
        }
    }
}

@Composable
private fun FilterSheet(
    peopleFilter : String,
    ageFilters   : Set<String>,
    typeFilters  : Set<String>,
    onPeople     : (String) -> Unit,
    onAgeToggle  : (String) -> Unit,
    onTypeToggle : (String) -> Unit,
    onClear      : () -> Unit,
    onApply      : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text("Фильтры", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // Количество людей (одиночный выбор)
        Text("Количество людей", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(peopleFilterOptions) { opt ->
                FilterChip(
                    selected = peopleFilter == opt,
                    onClick  = { onPeople(opt) },
                    label    = { Text(opt) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Возраст (множественный выбор)
        Text("Возраст", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ageFilterOptions) { opt ->
                FilterChip(
                    selected = opt in ageFilters,
                    onClick  = { onAgeToggle(opt) },
                    label    = { Text(opt) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Тип отдыха (множественный выбор)
        Text("Тип отдыха", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(typeFilterOptions) { opt ->
                FilterChip(
                    selected = opt in typeFilters,
                    onClick  = { onTypeToggle(opt) },
                    label    = { Text(opt) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick  = onClear,
                modifier = Modifier.weight(1f)
            ) { Text("Сбросить") }
            Button(
                onClick  = onApply,
                modifier = Modifier.weight(1f)
            ) { Text("Применить") }
        }
    }
}

@Composable
fun PlaceCard(
    place: Place,
    inRoute: Boolean,
    maxReached: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (place.imageUrl.isNotBlank()) {
                AsyncImage(
                    model          = place.imageUrl,
                    contentDescription = place.name,
                    contentScale   = ContentScale.Crop,
                    modifier       = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
            } else {
                Box(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentAlignment  = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier  = Modifier.size(48.dp),
                        tint      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = place.name,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = place.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    AssistChip(
                        onClick = {},
                        label   = { Text(place.category, style = MaterialTheme.typography.labelSmall) }
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text  = place.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick  = onToggle,
                        enabled  = inRoute || !maxReached,
                        colors   = if (inRoute) ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor   = MaterialTheme.colorScheme.onSecondaryContainer
                        ) else ButtonDefaults.buttonColors()
                    ) {
                        Icon(
                            if (inRoute) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (inRoute) "Добавлено" else "В маршрут")
                    }
                }
            }
        }
    }
}
