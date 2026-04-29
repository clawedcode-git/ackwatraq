package com.ackwatraq.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var intake by remember { mutableStateOf(0) }
    val dailyGoal = 2000

    Scaffold(
        topBar = { TopAppBar(title = { Text("💧 ackwatraq") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = {}, icon = { Text("🏠") }, label = { Text("Home") })
                NavigationBarItem(selected = false, onClick = { navController.navigate("history") }, icon = { Text("📊") }, label = { Text("History") })
                NavigationBarItem(selected = false, onClick = { navController.navigate("achievements") }, icon = { Text("🏆") }, label = { Text("Achievements") })
                NavigationBarItem(selected = false, onClick = { navController.navigate("settings") }, icon = { Text("⚙️") }, label = { Text("Settings") })
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Aqua-chan is ${if (intake >= dailyGoal) "happy! 🎉" else "thirsty! 😢"}", style = MaterialTheme.typography.headlineSmall)
            LinearProgressIndicator(
                progress = (intake.toFloat() / dailyGoal).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth().height(12.dp)
            )
            Text("$intake / $dailyGoal mL", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { intake += 250 }) { Text("+250mL") }
                Button(onClick = { intake += 500 }) { Text("+500mL") }
                Button(onClick = { intake += 1000 }) { Text("+1L") }
            }
        }
    }
}
