package com.ackwatraq.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavController
import com.ackwatraq.ui.utils.bouncyClick
import kotlin.math.roundToInt
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val intake by viewModel.intake.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val useMetric by viewModel.useMetric.collectAsState()
    val nickname by viewModel.nickname.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val currentSteps by viewModel.currentSteps.collectAsState()
    val unreadCount = notifications.count { !it.isRead }
    
    val rawProgress = if (dailyGoal > 0) intake.toFloat() / dailyGoal else 0f
    val progress = rawProgress.coerceIn(0f, 1f)
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "waterFillAnimation"
    )

    var showCustomDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var pendingLogAmount by remember { mutableStateOf<Int?>(null) }
    var showDrinkSheet by remember { mutableStateOf(false) }
    
    val unitString = if (useMetric) "mL" else "oz"
    val conversionFactor = if (useMetric) 1f else 29.5735f
    val displayIntake = (intake / conversionFactor).roundToInt()
    val displayGoal = (dailyGoal / conversionFactor).roundToInt()

    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good Morning,"
        in 12..16 -> "Good Afternoon,"
        else -> "Good Evening,"
    }
    
    val displayNickname = if (nickname.isNotBlank()) nickname else "Hydration Hero"

    // Konfetti state
    var showKonfetti by remember { mutableStateOf(false) }
    
    // We only want to trigger konfetti when intake transitions from < goal to >= goal.
    var previousIntake by remember { mutableStateOf(intake) }
    LaunchedEffect(intake) {
        if (intake >= dailyGoal && previousIntake < dailyGoal && dailyGoal > 0) {
            showKonfetti = true
        }
        previousIntake = intake
    }
    
    LaunchedEffect(showKonfetti) {
        if (showKonfetti) {
            kotlinx.coroutines.delay(3000)
            showKonfetti = false
        }
    }

    // Step tracking permission
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            viewModel.startStepTracking()
        }
    }
    
    DisposableEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            viewModel.startStepTracking()
        }
        onDispose {
            viewModel.stopStepTracking()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showKonfetti) {
            val party = Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xb48def, 0xf4306d),
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(0.5, 0.3)
            )
            KonfettiView(
                modifier = Modifier.fillMaxSize().zIndex(10f),
                parties = listOf(party)
            )
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(greeting, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = displayNickname,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showNotifications = true }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge { Text(unreadCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (showNotifications) {
                        AlertDialog(
                            onDismissRequest = { showNotifications = false },
                            title = { Text("Notifications") },
                            text = {
                                Column {
                                    if (notifications.isEmpty()) {
                                        Text("No notifications at this time.")
                                    } else {
                                        notifications.forEach { notification ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = notification.message,
                                                    style = if (notification.isRead) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (!notification.isRead) {
                                                    TextButton(onClick = { viewModel.markNotificationAsRead(notification.id) }) {
                                                        Text("Read")
                                                    }
                                                }
                                            }
                                            Divider()
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showNotifications = false }) { Text("Close") }
                            },
                            dismissButton = {
                                if (notifications.isNotEmpty()) {
                                    TextButton(onClick = { viewModel.clearNotifications() }) { Text("Clear All") }
                                }
                            }
                        )
                    }

                    // Circular Progress Centerpiece with Glowing Gradient Rings
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val secondaryColor = MaterialTheme.colorScheme.secondary
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokePx = with(this) { 20.dp.toPx() }
                            val sizePx = this.size.minDimension - strokePx
                            val offsetPx = strokePx / 2
                            
                            // Draw background track ring
                            drawCircle(
                                color = trackColor,
                                radius = sizePx / 2,
                                center = this.center,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokePx)
                            )
                            
                            // Draw glowing progress arc using linear gradient
                            drawArc(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(primaryColor, secondaryColor)
                                ),
                                startAngle = -90f,
                                sweepAngle = animatedProgress * 360f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = strokePx,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Target: $displayGoal $unitString", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.offset(x = 16.dp)
                            ) {
                                AnimatedContent(
                                    targetState = displayIntake,
                                    transitionSpec = {
                                        if (targetState > initialState) {
                                            (slideInVertically { height -> height } + fadeIn()) with (slideOutVertically { height -> -height } + fadeOut())
                                        } else {
                                            (slideInVertically { height -> -height } + fadeIn()) with (slideOutVertically { height -> height } + fadeOut())
                                        }.using(SizeTransform(clip = false))
                                    },
                                    label = "intakeAnimation"
                                ) { targetIntake ->
                                    Text("$targetIntake", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { showEditDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Total", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Text(unitString, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            AnimatedContent(
                                targetState = (rawProgress * 100).toInt(),
                                transitionSpec = {
                                    if (targetState > initialState) {
                                        (slideInVertically { height -> height } + fadeIn()) with (slideOutVertically { height -> -height } + fadeOut())
                                    } else {
                                        (slideInVertically { height -> -height } + fadeIn()) with (slideOutVertically { height -> height } + fadeOut())
                                    }.using(SizeTransform(clip = false))
                                },
                                label = "percentAnimation"
                            ) { targetPercent ->
                                Text("${targetPercent}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // Step counter smaller circle
                        val stepGoal = 10000
                        val stepProgress = (currentSteps.toFloat() / stepGoal).coerceIn(0f, 1f)
                        val animatedStepProgress by animateFloatAsState(
                            targetValue = stepProgress,
                            animationSpec = tween(durationMillis = 1000),
                            label = "stepFillAnimation"
                        )
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-8).dp, y = (-8).dp)
                                .size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxSize()
                            ) {}
                            CircularProgressIndicator(
                                progress = 1f,
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 8.dp
                            )
                            CircularProgressIndicator(
                                progress = animatedStepProgress,
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.tertiary,
                                strokeWidth = 8.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👣", fontSize = 16.sp)
                                Text("$currentSteps", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    // Aqua-chan Mascot Card
                    val progressPercent = rawProgress
                    val isDark = isSystemInDarkTheme()
                    
                    val mascotConfig = remember(progressPercent, isDark) {
                        when {
                            progressPercent <= 0.3f -> {
                                val bgColors = if (isDark) {
                                    listOf(Color(0xFF263238), Color(0xFF1E272C))
                                } else {
                                    listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC))
                                }
                                val txtColor = if (isDark) Color(0xFFB0BEC5) else Color(0xFF37474F)
                                MascotConfig(
                                    character = "😭",
                                    speech = "I'm dry... please log some water!",
                                    brush = Brush.linearGradient(bgColors),
                                    textColor = txtColor
                                )
                            }
                            progressPercent < 0.8f -> {
                                val bgColors = if (isDark) {
                                    listOf(Color(0xFF083344), Color(0xFF0A2540))
                                } else {
                                    listOf(Color(0xFFE8F0FE), Color(0xFFC2D9FF))
                                }
                                val txtColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF0F62FE)
                                MascotConfig(
                                    character = "😊",
                                    speech = "Doing great! Keep sipping!",
                                    brush = Brush.linearGradient(bgColors),
                                    textColor = txtColor
                                )
                            }
                            progressPercent < 1.0f -> {
                                val bgColors = if (isDark) {
                                    listOf(Color(0xFF1E1B4B), Color(0xFF311042))
                                } else {
                                    listOf(Color(0xFFE0F7FA), Color(0xFF80DEEA))
                                }
                                val txtColor = if (isDark) Color(0xFFD8B4FE) else Color(0xFF006064)
                                MascotConfig(
                                    character = "⚡",
                                    speech = "Almost there! You're super active!",
                                    brush = Brush.linearGradient(bgColors),
                                    textColor = txtColor
                                )
                            }
                            else -> {
                                MascotConfig(
                                    character = "👑",
                                    speech = "Goal achieved! You're a Hydration Hero!",
                                    brush = Brush.linearGradient(listOf(Color(0xFFFFE082), Color(0xFFFFB300))),
                                    textColor = Color(0xFF3E2723)
                                )
                            }
                        }
                    }

                    var mascotScale by remember { mutableStateOf(1f) }
                    LaunchedEffect(mascotConfig.character) {
                        mascotScale = 0.8f
                        kotlinx.coroutines.delay(100)
                        mascotScale = 1.1f
                        kotlinx.coroutines.delay(150)
                        mascotScale = 1.0f
                    }
                    val scaleAnimation by animateFloatAsState(
                        targetValue = mascotScale,
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                        ),
                        label = "mascotScale"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .bouncyClick {
                                mascotScale = 0.9f
                            }
                            .background(
                                brush = mascotConfig.brush,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .graphicsLayer {
                                        scaleX = scaleAnimation
                                        scaleY = scaleAnimation
                                    }
                                    .background(Color.White.copy(alpha = 0.25f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(mascotConfig.character, fontSize = 36.sp)
                            }
                            
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Aqua-chan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = mascotConfig.textColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                AnimatedContent(
                                    targetState = mascotConfig.speech,
                                    transitionSpec = {
                                        (fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { it / 2 }) with
                                                (fadeOut(animationSpec = tween(200)) + slideOutVertically(animationSpec = tween(200)) { -it / 2 })
                                    },
                                    label = "speechTransition"
                                ) { targetText ->
                                    Text(
                                        text = targetText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = mascotConfig.textColor.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Intake Buttons with Premium Gradients and Shadow Shadows
                    Text("Quick Add", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val presets = listOf(
                            Triple(if (useMetric) 250 else 237, if (useMetric) "+250" else "+8", MaterialTheme.colorScheme.primary),
                            Triple(if (useMetric) 500 else 503, if (useMetric) "+500" else "+17", MaterialTheme.colorScheme.primary),
                            Triple(if (useMetric) 1000 else 1005, if (useMetric) "+1L" else "+34", MaterialTheme.colorScheme.primary)
                        )
                        
                        presets.forEach { (amount, labelText, color) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .bouncyClick {
                                        pendingLogAmount = amount
                                        showDrinkSheet = true
                                    }
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(labelText, style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold) 
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .bouncyClick { showCustomDialog = true }
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.primary)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ ?", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold) 
                        }
                    }

                    if (showCustomDialog) {
                        var customAmount by remember { mutableStateOf("") }
                        AlertDialog(
                            onDismissRequest = { showCustomDialog = false },
                            title = { Text("Custom Amount") },
                            text = {
                                OutlinedTextField(
                                    value = customAmount,
                                    onValueChange = { newValue -> if (newValue.all { it.isDigit() }) customAmount = newValue },
                                    label = { Text("Enter $unitString") },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    singleLine = true
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val amount = customAmount.toIntOrNull()
                                        if (amount != null && amount > 0) {
                                            pendingLogAmount = (amount * conversionFactor).roundToInt()
                                            showCustomDialog = false
                                            showDrinkSheet = true
                                        }
                                    }
                                ) { Text("Add") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showCustomDialog = false }) { Text("Cancel") }
                            }
                        )
                    }

                    if (showEditDialog) {
                        var editAmount by remember { mutableStateOf(displayIntake.toString()) }
                        AlertDialog(
                            onDismissRequest = { showEditDialog = false },
                            title = { Text("Edit Total Intake") },
                            text = {
                                OutlinedTextField(
                                    value = editAmount,
                                    onValueChange = { newValue -> if (newValue.all { it.isDigit() }) editAmount = newValue },
                                    label = { Text("Total $unitString") },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    singleLine = true
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val amount = editAmount.toIntOrNull()
                                        if (amount != null && amount >= 0) {
                                            viewModel.editTotalIntake((amount * conversionFactor).roundToInt())
                                            showEditDialog = false
                                        }
                                    }
                                ) { Text("Save") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
                            }
                        )
                    }
                }
            }
        )

        val currentLogAmount = pendingLogAmount
        if (showDrinkSheet && currentLogAmount != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            
            ModalBottomSheet(
                onDismissRequest = {
                    showDrinkSheet = false
                    pendingLogAmount = null
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Select Beverage",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Different drinks hydrate at different rates. Select what you just drank to log its effective hydration.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    val drinkOptions = remember {
                        listOf(
                            DrinkTypeOption("Water", "Pure Water", "💧", 1.0f, "Optimal hydration", Color(0xFF00E5FF), Color(0xFF0F62FE)),
                            DrinkTypeOption("Tea", "Herbal Tea", "🍵", 0.95f, "Gentle hydration (95% eff.)", Color(0xFF81C784), Color(0xFF2E7D32)),
                            DrinkTypeOption("Sports Drink", "Sports Drink", "⚡", 1.0f, "Electrolytes & hydration", Color(0xFFFFB74D), Color(0xFFE65100)),
                            DrinkTypeOption("Coffee", "Coffee", "☕", 0.8f, "Mild diuretic (80% eff.)", Color(0xFFA1887F), Color(0xFF5D4037)),
                            DrinkTypeOption("Soda / Juice", "Soda & Juice", "🥤", 0.6f, "Sugar & caffeine (60% eff.)", Color(0xFFF06292), Color(0xFFC2185B))
                        )
                    }
                    
                    drinkOptions.forEach { option ->
                        val isDark = isSystemInDarkTheme()
                        val iconBg = if (isDark) option.darkColor.copy(alpha = 0.15f) else option.lightColor.copy(alpha = 0.1f)
                        
                        val rawAmt = currentLogAmount
                        val effectiveAmt = Math.round(rawAmt * option.coefficient)
                        val displayEffective = (effectiveAmt / conversionFactor).roundToInt()
                        val displayRaw = (rawAmt / conversionFactor).roundToInt()
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bouncyClick {
                                    viewModel.addWater(rawAmt, option.id)
                                    showDrinkSheet = false
                                    pendingLogAmount = null
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(iconBg, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(option.emoji, fontSize = 24.sp)
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = option.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "+$displayEffective $unitString",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "from $displayRaw $unitString",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class MascotConfig(
    val character: String,
    val speech: String,
    val brush: Brush,
    val textColor: Color
)

private data class DrinkTypeOption(
    val id: String,
    val name: String,
    val emoji: String,
    val coefficient: Float,
    val description: String,
    val darkColor: Color,
    val lightColor: Color
)
