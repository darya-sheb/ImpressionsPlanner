package com.hse.impressionsplanner

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Constructor : Screen("constructor", "Конструктор", Icons.Default.Explore)
    object Impressions : Screen("impressions", "Впечатления", Icons.Default.AutoAwesome)
    object Diary       : Screen("diary",       "Дневник",     Icons.AutoMirrored.Filled.MenuBook)
    object Profile     : Screen("profile",     "Профиль",     Icons.Default.Person)
}
