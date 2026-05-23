package com.ackwatraq.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Casino

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.clickable
import com.ackwatraq.ui.settings.SettingsViewModel
import com.ackwatraq.domain.model.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel) {
    val prefs by viewModel.userPreferences.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Appearance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎨 Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.setTheme(AppTheme.SYSTEM) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (prefs.theme == AppTheme.SYSTEM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (prefs.theme == AppTheme.SYSTEM) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("System") }
        
                        Button(
                            onClick = { viewModel.setTheme(AppTheme.LIGHT) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (prefs.theme == AppTheme.LIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (prefs.theme == AppTheme.LIGHT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("Light") }
        
                        Button(
                            onClick = { viewModel.setTheme(AppTheme.DARK) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (prefs.theme == AppTheme.DARK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (prefs.theme == AppTheme.DARK) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("Dark") }
                    }
                }
            }

            // Nickname Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                var showNicknameDialog by remember { mutableStateOf(false) }
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🧑‍🤝‍🧑 Nickname", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Row {
                            IconButton(onClick = {
                                val randomNames = listOf("Hydro Hero", "Aqua Ace", "Water Warrior", "Splash Star")
                                viewModel.setNickname(randomNames.random())
                            }) {
                                Icon(Icons.Default.Casino, contentDescription = "Random Nickname")
                            }
                            IconButton(onClick = { showNicknameDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Nickname")
                            }
                        }
                    }
                    if (prefs.nickname.isNotEmpty()) {
                        Text(prefs.nickname, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
                    }
                }
                if (showNicknameDialog) {
                    var nicknameInput by remember { mutableStateOf(prefs.nickname) }
                    AlertDialog(
                        onDismissRequest = { showNicknameDialog = false },
                        title = { Text("Set Nickname") },
                        text = {
                            OutlinedTextField(
                                value = nicknameInput,
                                onValueChange = { if (it.length <= 24) nicknameInput = it },
                                label = { Text("Nickname") },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.setNickname(nicknameInput)
                                showNicknameDialog = false
                            }) { Text("Save") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showNicknameDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }

            // Hydration Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    var showGoalDialog by remember { mutableStateOf(false) }
        
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGoalDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎯 Daily Goal", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text("${prefs.dailyGoalMl} mL", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
        
                    if (showGoalDialog) {
                        var goalAmount by remember { mutableStateOf(prefs.dailyGoalMl.toString()) }
                        AlertDialog(
                            onDismissRequest = { showGoalDialog = false },
                            title = { Text("Set Daily Goal") },
                            text = {
                                OutlinedTextField(
                                    value = goalAmount,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() }) {
                                            goalAmount = newValue
                                        }
                                    },
                                    label = { Text("Goal in mL") },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    singleLine = true
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val amount = goalAmount.toIntOrNull()
                                        if (amount != null && amount > 0) {
                                            viewModel.setDailyGoal(amount)
                                            showGoalDialog = false
                                        }
                                    },
                                    enabled = goalAmount.isNotEmpty() && (goalAmount.toIntOrNull() ?: 0) > 0
                                ) {
                                    Text("Save")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showGoalDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp).alpha(0.5f))
        
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏰ Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                        ) { isGranted: Boolean ->
                            viewModel.setRemindersEnabled(isGranted)
                        }
                        
                        androidx.compose.material3.Switch(
                            checked = prefs.remindersEnabled,
                            onCheckedChange = { enabled -> 
                                if (enabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setRemindersEnabled(enabled) 
                                }
                            }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp).alpha(0.5f))
        
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚖️ Metric Units (mL)", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        androidx.compose.material3.Switch(
                            checked = prefs.useMetric,
                            onCheckedChange = { viewModel.setUseMetric(it) }
                        )
                    }
                }
            }
        }
    }
}
