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
import androidx.compose.foundation.verticalScroll
import com.ackwatraq.ui.settings.SettingsViewModel
import com.ackwatraq.domain.model.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel) {
    val prefs by viewModel.userPreferences.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        val scrollState = androidx.compose.foundation.rememberScrollState()
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            // User Preferences Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                var showNicknameDialog by remember { mutableStateOf(false) }
                var showGoalDialog by remember { mutableStateOf(false) }
                var showQuietHoursDialog by remember { mutableStateOf(false) }

                Column(modifier = Modifier.padding(12.dp)) {
                    Text("⚙️ User Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Nickname
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column {
                            Text("🧑‍🤝‍🧑 Nickname", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            if (prefs.nickname.isNotEmpty()) {
                                Text(prefs.nickname, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Row {
                            IconButton(onClick = {
                                val randomNames = listOf("Hydro Hero", "Aqua Ace", "Water Warrior", "Splash Star")
                                viewModel.setNickname(randomNames.random())
                            }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Casino, contentDescription = "Random Nickname", modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { showNicknameDialog = true }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Nickname", modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp).alpha(0.5f))

                    // Daily Goal
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showGoalDialog = true }.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎯 Daily Goal", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text("${prefs.dailyGoalMl} mL", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp).alpha(0.5f))

                    // Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏰ Reminders", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp).alpha(0.5f))
        
                    // Metric Units
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚖️ Metric Units (mL)", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        androidx.compose.material3.Switch(
                            checked = prefs.useMetric,
                            onCheckedChange = { viewModel.setUseMetric(it) },
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp).alpha(0.5f))

                    // Quiet Hours
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showQuietHoursDialog = true }.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🤫 Quiet Hours", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        val startFmt = String.format("%02d:00", prefs.quietHoursStart)
                        val endFmt = String.format("%02d:00", prefs.quietHoursEnd)
                        Text("$startFmt to $endFmt", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Dialogs
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

                if (showGoalDialog) {
                    var goalAmount by remember { mutableStateOf(prefs.dailyGoalMl.toString()) }
                    AlertDialog(
                        onDismissRequest = { showGoalDialog = false },
                        title = { Text("Set Daily Goal") },
                        text = {
                            OutlinedTextField(
                                value = goalAmount,
                                onValueChange = { newValue -> if (newValue.all { it.isDigit() }) goalAmount = newValue },
                                label = { Text("Goal in mL") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
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
                            ) { Text("Save") }
                        },
                        dismissButton = { TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") } }
                    )
                }

                if (showQuietHoursDialog) {
                    var startVal by remember { mutableStateOf(prefs.quietHoursStart.toFloat()) }
                    var endVal by remember { mutableStateOf(prefs.quietHoursEnd.toFloat()) }
                    AlertDialog(
                        onDismissRequest = { showQuietHoursDialog = false },
                        title = { Text("Set Quiet Hours") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Quiet hours define the window when reminder notifications are silenced.", style = MaterialTheme.typography.bodyMedium)
                                
                                Column {
                                    Text(
                                        text = String.format("Start Hour: %02d:00", startVal.toInt()),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Slider(
                                        value = startVal,
                                        onValueChange = { startVal = it },
                                        valueRange = 0f..23f,
                                        steps = 22
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = String.format("End Hour: %02d:00", endVal.toInt()),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Slider(
                                        value = endVal,
                                        onValueChange = { endVal = it },
                                        valueRange = 0f..23f,
                                        steps = 22
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.setQuietHours(startVal.toInt(), endVal.toInt())
                                showQuietHoursDialog = false
                            }) { Text("Save") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showQuietHoursDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }

            // App Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📱 App Settings", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Theme", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                        Button(
                            onClick = { viewModel.setTheme(AppTheme.SYSTEM) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (prefs.theme == AppTheme.SYSTEM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (prefs.theme == AppTheme.SYSTEM) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("System", style = MaterialTheme.typography.bodySmall) }
        
                        Button(
                            onClick = { viewModel.setTheme(AppTheme.LIGHT) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (prefs.theme == AppTheme.LIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (prefs.theme == AppTheme.LIGHT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("Light", style = MaterialTheme.typography.bodySmall) }
        
                        Button(
                            onClick = { viewModel.setTheme(AppTheme.DARK) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (prefs.theme == AppTheme.DARK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (prefs.theme == AppTheme.DARK) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("Dark", style = MaterialTheme.typography.bodySmall) }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp).alpha(0.5f))

                    Text("Data & Backup", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
                    ) { uri -> if (uri != null) viewModel.exportData(context, uri) }
                    
                    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
                    ) { uri -> if (uri != null) viewModel.importData(context, uri) }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Button(
                            onClick = { exportLauncher.launch("ackwatraq_backup.json") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Export") }
                        
                        Button(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Import") }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
