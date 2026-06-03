package com.hse.impressionsplanner

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Constructor : Screen("constructor", "Конструктор", Icons.Default.Search)
    object Impressions : Screen("impressions", "Впечатления", Icons.Default.Favorite)
    object Diary : Screen("diary", "Дневник", Icons.Default.List)
    object Profile : Screen("profile", "Профиль", Icons.Default.Person)
}