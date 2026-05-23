package com.ackwatraq.ui.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    val (emoji, bgGradient) = getAchievementStyle(achievement.title, achievement.unlocked)
    
    val textColor = if (achievement.unlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val iconAlpha = if (achievement.unlocked) 1f else 0.4f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f) // slightly taller for better text layout
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = bgGradient,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(100f, 300f)
                )
            )
            .let { 
                if (achievement.unlocked) {
                    it.border(
                        width = 2.dp, 
                        color = Color.White.copy(alpha = 0.4f), 
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                    )
                } else it 
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (achievement.unlocked) 0.25f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji, 
                    style = MaterialTheme.typography.headlineMedium, 
                    modifier = Modifier.alpha(iconAlpha)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = achievement.title, 
                style = MaterialTheme.typography.labelSmall, 
                color = textColor, 
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = achievement.description, 
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), 
                color = textColor.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 12.sp
            )
        }
    }
}

fun getAchievementStyle(title: String, unlocked: Boolean): Pair<String, List<Color>> {
    if (!unlocked) return "🔒" to listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD)) // Gray out locked achievements
    
    // Vibrant Gradients for unlocked achievements
    return when {
        title.contains("First", ignoreCase = true) -> "💧" to listOf(Color(0xFF64B5F6), Color(0xFF1976D2))
        title.contains("Goal", ignoreCase = true) -> "🎯" to listOf(Color(0xFF81C784), Color(0xFF388E3C))
        title.contains("Streak", ignoreCase = true) -> "🔥" to listOf(Color(0xFFFFB74D), Color(0xFFF57C00))
        title.contains("Week", ignoreCase = true) -> "📅" to listOf(Color(0xFFBA68C8), Color(0xFF7B1FA2))
        title.contains("Over", ignoreCase = true) -> "🚀" to listOf(Color(0xFFF06292), Color(0xFFC2185B))
        title.contains("Early", ignoreCase = true) -> "🌅" to listOf(Color(0xFFFFD54F), Color(0xFFFFA000))
        title.contains("Night", ignoreCase = true) -> "🦉" to listOf(Color(0xFF7986CB), Color(0xFF303F9F))
        title.contains("Ocean", ignoreCase = true) -> "🐋" to listOf(Color(0xFF4DD0E1), Color(0xFF0097A7))
        title.contains("Camel", ignoreCase = true) -> "🐫" to listOf(Color(0xFFFF8A65), Color(0xFFE64A19))
        else -> "🏆" to listOf(Color(0xFFFFD54F), Color(0xFFF57F17))
    }
}
