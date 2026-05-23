package com.ackwatraq.ui.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ackwatraq.domain.model.Achievement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(navController: NavController, viewModel: AchievementsViewModel) {
    val achievements by viewModel.achievements.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    val volume by viewModel.lifetimeVolume.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Trophy Room") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔥 Streak", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$streak Days", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏆 Total", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        val volumeLiters = volume / 1000f
                        val displayVolume = if (volumeLiters % 1 == 0f) volumeLiters.toInt().toString() else String.format("%.1f", volumeLiters)
                        Text("$displayVolume L", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(achievements) { ach ->
                    AchievementBadge(achievement = ach)
                }
            }
        }
    }
}

@Composable
fun AchievementBadge(achievement: Achievement) {
    val (emoji, bgTint) = getAchievementStyle(achievement.title, achievement.unlocked)
    
    val cardColor = if (achievement.unlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (achievement.unlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val iconAlpha = if (achievement.unlocked) 1f else 0.3f

    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = if (achievement.unlocked) 6.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgTint.copy(alpha = if (achievement.unlocked) 0.2f else 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, style = MaterialTheme.typography.titleLarge, modifier = Modifier.alpha(iconAlpha))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = achievement.title, 
                style = MaterialTheme.typography.labelSmall, 
                color = textColor, 
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = achievement.description, 
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp), 
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 10.sp
            )
        }
    }
}

fun getAchievementStyle(title: String, unlocked: Boolean): Pair<String, Color> {
    if (!unlocked) return "🔒" to Color.Gray
    
    return when {
        title.contains("First", ignoreCase = true) -> "💧" to Color(0xFF2196F3)
        title.contains("Goal", ignoreCase = true) -> "🎯" to Color(0xFF4CAF50)
        title.contains("Streak", ignoreCase = true) -> "🔥" to Color(0xFFFF9800)
        title.contains("Week", ignoreCase = true) -> "📅" to Color(0xFF9C27B0)
        title.contains("Over", ignoreCase = true) -> "🚀" to Color(0xFFE91E63)
        title.contains("Early", ignoreCase = true) -> "🌅" to Color(0xFFFFC107)
        title.contains("Night", ignoreCase = true) -> "🦉" to Color(0xFF3F51B5)
        title.contains("Ocean", ignoreCase = true) -> "🐋" to Color(0xFF00BCD4)
        title.contains("Camel", ignoreCase = true) -> "🐫" to Color(0xFFFF5722)
        else -> "🏆" to Color(0xFFFFC107)
    }
}
