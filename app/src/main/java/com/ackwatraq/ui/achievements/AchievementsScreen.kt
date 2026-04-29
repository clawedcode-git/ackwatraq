package com.ackwatraq.ui.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("🏆 Achievements") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🎖️ First Sip — Log your first intake", style = MaterialTheme.typography.bodyLarge)
            Text("💧 2L Club — Drink 2L in a day", style = MaterialTheme.typography.bodyLarge)
            Text("🔥 3-Day Streak — Meet goal 3 days", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
