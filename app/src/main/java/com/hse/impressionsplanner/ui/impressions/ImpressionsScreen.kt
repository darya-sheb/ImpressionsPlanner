package com.hse.impressionsplanner.ui.impressions

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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
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
import com.hse.impressionsplanner.data.Route

// Фильтры впечатлений (аналогично конструктору)
private val impressionTypeOptions = listOf(
    "Активный", "Спокойный", "Культура", "Гастрономия", "Природа", "Исторический"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpressionsScreen(viewModel: ImpressionsViewModel = viewModel()) {
    val routes       by viewModel.routes.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val savedToDiary by viewModel.savedToDiary.collectAsState()

    var searchQuery   by remember { mutableStateOf("") }
    var selectedRoute by remember { mutableStateOf<Route?>(null) }
    var showFilters   by remember { mutableStateOf(false) }
    var typeFilters   by remember { mutableStateOf<Set<String>>(emptySet()) }

    val context           = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(savedToDiary) {
        if (savedToDiary) snackbarHostState.showSnackbar("Маршрут сохранён в дневник")
    }

    val filteredRoutes = remember(routes, searchQuery, typeFilters) {
        var result = routes
        if (typeFilters.isNotEmpty()) {
            result = result.filter { route -> route.categories.any { it in typeFilters } }
        }
        if (searchQuery.isNotBlank()) {
            result = result.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
        result
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text       = "Впечатления",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Поиск + кнопка Фильтры
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Поиск по маршрутам") },
                    leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon  = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Очистить")
                            }
                        }
                    },
                    singleLine = true,
                    shape      = RoundedCornerShape(12.dp),
                    modifier   = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                BadgedBox(
                    badge = {
                        if (typeFilters.isNotEmpty()) {
                            Badge { Text("${typeFilters.size}") }
                        }
                    }
                ) {
                    FilledTonalIconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтры")
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredRoutes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text  = "Ничего не найдено",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRoutes, key = { it.id }) { route ->
                        RouteCard(
                            route         = route,
                            isSaved       = viewModel.isSaved(route.id),
                            onFavoriteClick = { viewModel.toggleSave(route.id) },
                            onCardClick   = { selectedRoute = route }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text("Фильтры", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text("Тип маршрута", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(impressionTypeOptions) { opt ->
                        FilterChip(
                            selected = opt in typeFilters,
                            onClick  = {
                                val s = typeFilters.toMutableSet()
                                if (!s.add(opt)) s.remove(opt)
                                typeFilters = s
                            },
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
                        onClick  = { typeFilters = emptySet() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Сбросить") }
                    Button(
                        onClick  = { showFilters = false },
                        modifier = Modifier.weight(1f)
                    ) { Text("Применить") }
                }
            }
        }
    }

    // Детальный экран маршрута
    selectedRoute?.let { route ->
        ModalBottomSheet(
            onDismissRequest = { selectedRoute = null },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            RouteDetailSheet(
                route         = route,
                isSaved       = viewModel.isSaved(route.id),
                onFavoriteToggle = { viewModel.toggleSave(route.id) },
                onSaveToDiary = {
                    viewModel.saveRouteToDiary(route)
                    selectedRoute = null
                },
                onOpenSource  = { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                },
                onClose       = { selectedRoute = null }
            )
        }
    }
}

@Composable
fun RouteCard(
    route: Route,
    isSaved: Boolean,
    onFavoriteClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick   = onCardClick
    ) {
        Column {
            Box {
                if (route.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model              = route.imageUrl,
                        contentDescription = route.name,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                } else {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text       = route.name,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(route.duration, style = MaterialTheme.typography.labelSmall) })
                    AssistChip(onClick = {}, label = { Text("${route.placeCount} мест", style = MaterialTheme.typography.labelSmall) })
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text     = route.description,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
fun RouteDetailSheet(
    route: Route,
    isSaved: Boolean,
    onFavoriteToggle: () -> Unit,
    onSaveToDiary: () -> Unit,
    onOpenSource: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = route.name,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть")
            }
        }

        Spacer(Modifier.height(8.dp))

        if (route.imageUrl.isNotBlank()) {
            AsyncImage(
                model              = route.imageUrl,
                contentDescription = route.name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(12.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(route.duration) })
            AssistChip(onClick = {}, label = { Text("${route.placeCount} мест") })
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text  = route.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick  = onSaveToDiary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Сохранить в дневник")
        }

        if (route.sourceUrl.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick  = { onOpenSource(route.sourceUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Перейти к источнику")
            }
        }
    }
}
