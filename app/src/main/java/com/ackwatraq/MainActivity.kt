package com.ackwatraq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ackwatraq.ui.achievements.AchievementsScreen
import com.ackwatraq.ui.home.HomeScreen
import com.ackwatraq.ui.home.HomeViewModel
import com.ackwatraq.ui.history.HistoryScreen
import com.ackwatraq.ui.history.HistoryViewModel
import com.ackwatraq.ui.settings.SettingsScreen
import com.ackwatraq.ui.theme.AckwatraqTheme

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Settings
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size

import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape

import com.ackwatraq.ui.achievements.AchievementsViewModel
import com.ackwatraq.ui.settings.SettingsViewModel
import com.ackwatraq.domain.model.AppTheme
import com.ackwatraq.domain.model.UserPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = application as AckwatraqApplication
            val repository = app.repository
            val prefs by repository.userPreferencesFlow.collectAsState(initial = UserPreferences())
            
            val isDarkTheme = when (prefs.theme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            
            AckwatraqTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    val repository = (application as AckwatraqApplication).repository
                    val stepRepository = (application as AckwatraqApplication).stepRepository

                    val factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                                return HomeViewModel(repository, stepRepository) as T
                            }
                            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                                return HistoryViewModel(repository) as T
                            }
                            if (modelClass.isAssignableFrom(AchievementsViewModel::class.java)) {
                                return AchievementsViewModel(repository) as T
                            }
                            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                                return SettingsViewModel(repository) as T
                            }
                            throw IllegalArgumentException("Unknown ViewModel class")
                        }
                    }

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    Scaffold(
                        bottomBar = {
                            if (currentRoute != "splash") {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                        .fillMaxWidth()
                                ) {
                                    NavigationBar(
                                        modifier = Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(32.dp)),
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                                        tonalElevation = 8.dp
                                    ) {
                                        NavigationBarItem(
                                            selected = currentRoute == "home",
                                            onClick = { 
                                                navController.navigate("home") { 
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true; restoreState = true 
                                                } 
                                            },
                                            icon = { FancyGradientIcon(Icons.Rounded.Home, currentRoute == "home") },
                                            label = { Text("Home") }
                                        )
                                        NavigationBarItem(
                                            selected = currentRoute == "history",
                                            onClick = { 
                                                navController.navigate("history") { 
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true; restoreState = true 
                                                } 
                                            },
                                            icon = { FancyGradientIcon(Icons.Rounded.Timeline, currentRoute == "history") },
                                            label = { Text("History") }
                                        )
                                        NavigationBarItem(
                                            selected = currentRoute == "achievements",
                                            onClick = { 
                                                navController.navigate("achievements") { 
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true; restoreState = true 
                                                } 
                                            },
                                            icon = { FancyGradientIcon(Icons.Rounded.EmojiEvents, currentRoute == "achievements") },
                                            label = { Text("Trophies") }
                                        )
                                        NavigationBarItem(
                                            selected = currentRoute == "settings",
                                            onClick = { 
                                                navController.navigate("settings") { 
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true; restoreState = true 
                                                } 
                                            },
                                            icon = { FancyGradientIcon(Icons.Rounded.Settings, currentRoute == "settings") },
                                            label = { Text("Settings") }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        val paddingMod = if (currentRoute == "splash") Modifier else Modifier.padding(innerPadding)
                        NavHost(
                            navController = navController, 
                            startDestination = "splash",
                            modifier = paddingMod
                        ) {
                            composable("splash") {
                                com.ackwatraq.ui.splash.SplashScreen(navController)
                            }
                            composable("home") { 
                                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                                HomeScreen(navController, homeViewModel) 
                            }
                            composable("history") { 
                                val historyViewModel: HistoryViewModel = viewModel(factory = factory)
                                HistoryScreen(navController, historyViewModel) 
                            }
                            composable("achievements") { 
                                val achievementsViewModel: AchievementsViewModel = viewModel(factory = factory)
                                AchievementsScreen(navController, achievementsViewModel) 
                            }
                            composable("settings") { 
                                val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
                                SettingsScreen(navController, settingsViewModel) 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FancyGradientIcon(imageVector: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean) {
    val gradientColors = if (selected) {
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    } else {
        listOf(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
    
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier
            .size(26.dp)
            .graphicsLayer(alpha = 0.99f)
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(gradientColors),
                        blendMode = androidx.compose.ui.graphics.BlendMode.SrcAtop
                    )
                }
            }
    )
}
