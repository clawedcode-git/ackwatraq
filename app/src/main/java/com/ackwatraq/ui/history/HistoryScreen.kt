package com.ackwatraq.ui.history

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, viewModel: HistoryViewModel) {
    val historyData by viewModel.historyData.collectAsState()
    val rangeType by viewModel.selectedRangeType.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDateRangePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    val startMillis = datePickerState.selectedStartDateMillis
                    val endMillis = datePickerState.selectedEndDateMillis
                    if (startMillis != null && endMillis != null) {
                        val start = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                        val end = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.setCustomRange(start, end)
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = datePickerState,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("History") }) }
    ) { padding ->
        val drinkBreakdown by viewModel.drinkBreakdown.collectAsState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val titleText = when (rangeType) {
                HistoryRangeType.DAYS_7 -> "Last 7 Days Intake"
                HistoryRangeType.DAYS_30 -> "Last 30 Days Intake"
                HistoryRangeType.CUSTOM -> "Custom Date Range"
            }
            Text(titleText, style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = rangeType == HistoryRangeType.DAYS_7,
                    onClick = { viewModel.setRangeType(HistoryRangeType.DAYS_7) },
                    label = { Text("7 Days") }
                )
                FilterChip(
                    selected = rangeType == HistoryRangeType.DAYS_30,
                    onClick = { viewModel.setRangeType(HistoryRangeType.DAYS_30) },
                    label = { Text("30 Days") }
                )
                FilterChip(
                    selected = rangeType == HistoryRangeType.CUSTOM,
                    onClick = { showDatePicker = true },
                    label = { Text("Custom") },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select Dates", modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                )
            }

            if (historyData.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val useMetric by viewModel.useMetric.collectAsState()
                val unitString = if (useMetric) "mL" else "oz"
                val conversionFactor = if (useMetric) 1f else 29.5735f
                
                val totalIntake = historyData.values.sum()
                val avgIntake = if (historyData.isNotEmpty()) totalIntake / historyData.size else 0
                val displayTotal = (totalIntake / conversionFactor).toInt()
                val displayAvg = (avgIntake / conversionFactor).toInt()
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("Total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            Text("$displayTotal $unitString", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("Daily Avg", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            Text("$displayAvg $unitString", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                
                // Dynamic Beverage Distribution Chart
                DrinkBreakdownChart(
                    breakdown = drinkBreakdown,
                    useMetric = useMetric,
                    conversionFactor = conversionFactor,
                    unitString = unitString
                )
                
                // Weekly Bar Chart
                val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
                val secondaryColor = MaterialTheme.colorScheme.tertiary.toArgb()
                
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    factory = { context ->
                        BarChart(context).apply {
                            description.isEnabled = false
                            setDrawGridBackground(false)
                            axisRight.isEnabled = false
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.setDrawGridLines(false)
                            xAxis.textColor = textColor
                            axisLeft.axisMinimum = 0f
                            axisLeft.textColor = textColor
                            legend.textColor = textColor
                            animateY(1000)
                        }
                    },
                    update = { chart ->
                        chart.xAxis.textColor = textColor
                        chart.axisLeft.textColor = textColor
                        chart.legend.textColor = textColor
                        
                        val sortedEntries = historyData.entries.sortedBy { it.key }
                        val barEntries = sortedEntries.mapIndexed { index, entry ->
                            val displayValue = (entry.value / conversionFactor).toFloat()
                            BarEntry(index.toFloat(), displayValue)
                        }
                        
                        val formatter = if (sortedEntries.size <= 7) {
                            DateTimeFormatter.ofPattern("E")
                        } else {
                            DateTimeFormatter.ofPattern("MMM dd")
                        }
                        val labels = sortedEntries.map { it.key.format(formatter) }
                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        chart.xAxis.labelCount = Math.min(labels.size, 7)

                        val dataSet = BarDataSet(barEntries, "Water Intake ($unitString)").apply {
                            setGradientColor(primaryColor, secondaryColor)
                            valueTextSize = 10f
                            valueTextColor = textColor
                            setDrawValues(sortedEntries.size <= 14)
                        }

                        chart.data = BarData(dataSet)
                        chart.invalidate()
                    }
                )
            }
        }
    }
}

@Composable
fun DrinkBreakdownChart(
    breakdown: Map<String, Int>,
    useMetric: Boolean,
    conversionFactor: Float,
    unitString: String
) {
    if (breakdown.isEmpty() || breakdown.values.sum() == 0) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No drinks logged in this range", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val total = breakdown.values.sum().toFloat()
    
    val drinkMeta = remember {
        mapOf(
            "Water" to Pair("Pure Water 💧", Color(0xFF00E5FF).toArgb()),
            "Tea" to Pair("Herbal Tea 🍵", Color(0xFF81C784).toArgb()),
            "Sports Drink" to Pair("Sports Drink ⚡", Color(0xFFFFB74D).toArgb()),
            "Coffee" to Pair("Coffee ☕", Color(0xFFA1887F).toArgb()),
            "Soda / Juice" to Pair("Soda & Juice 🥤", Color(0xFFF06292).toArgb())
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Beverage Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        val strokeWidth = 14.dp.toPx()
                        
                        breakdown.forEach { (type, amount) ->
                            val sweepAngle = (amount / total) * 360f
                            val colorInt = drinkMeta[type]?.second ?: Color(0xFF9E9E9E).toArgb()
                            
                            drawArc(
                                color = androidx.compose.ui.graphics.Color(colorInt),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            startAngle += sweepAngle
                        }
                    }
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        val displayTotal = (total / conversionFactor).toInt()
                        Text("${displayTotal}", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text(unitString, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    breakdown.forEach { (type, amount) ->
                        val pct = (amount / total * 100).toInt()
                        val meta = drinkMeta[type] ?: Pair(type, Color(0xFF9E9E9E).toArgb())
                        val displayAmount = (amount / conversionFactor).toInt()
                        
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(androidx.compose.ui.graphics.Color(meta.second), shape = CircleShape)
                                )
                                Text(
                                    text = meta.first,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            }
                            Text(
                                text = "$pct% ($displayAmount $unitString)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
