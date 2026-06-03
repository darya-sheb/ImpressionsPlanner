package com.hse.impressionsplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hse.impressionsplanner.ui.auth.AuthViewModel
import com.hse.impressionsplanner.ui.constructor.ConstructorScreen
import com.hse.impressionsplanner.ui.diary.DiaryScreen
import com.hse.impressionsplanner.ui.impressions.ImpressionsScreen
import com.hse.impressionsplanner.ui.profile.ProfileScreen
import com.hse.impressionsplanner.ui.theme.ImpressionsPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImpressionsPlannerTheme {
                val authViewModel: AuthViewModel = viewModel()
                val navController = rememberNavController()
                val tabs = listOf(
                    Screen.Constructor,
                    Screen.Impressions,
                    Screen.Diary,
                    Screen.Profile
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route
                            tabs.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Constructor.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Constructor.route) { ConstructorScreen() }
                        composable(Screen.Impressions.route) { ImpressionsScreen() }
                        composable(Screen.Diary.route) { DiaryScreen() }
                        composable(Screen.Profile.route) { ProfileScreen(authViewModel) }
                    }
                }
            }
        }
    }
}