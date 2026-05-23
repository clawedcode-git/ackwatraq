package com.ackwatraq.ui.history

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            val titleText = when (rangeType) {
                HistoryRangeType.DAYS_7 -> "Last 7 Days Intake"
                HistoryRangeType.DAYS_30 -> "Last 30 Days Intake"
                HistoryRangeType.CUSTOM -> "Custom Date Range"
            }
            Text(titleText, style = MaterialTheme.typography.titleLarge)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), 
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

            Spacer(modifier = Modifier.height(8.dp))

            if (historyData.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
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
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
                val secondaryColor = MaterialTheme.colorScheme.tertiary.toArgb()
                
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
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
                            setDrawValues(sortedEntries.size <= 14) // Hide values on top of bars if too crowded
                        }

                        chart.data = BarData(dataSet)
                        chart.invalidate()
                    }
                )
            }
        }
    }
}
