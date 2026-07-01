package com.ackwatraq.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackwatraq.data.repository.WaterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

enum class HistoryRangeType {
    DAYS_7, DAYS_30, CUSTOM
}

class HistoryViewModel(private val repository: WaterRepository) : ViewModel() {
    private val _historyData = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val historyData: StateFlow<Map<LocalDate, Int>> = _historyData.asStateFlow()

    private val _drinkBreakdown = MutableStateFlow<Map<String, Int>>(emptyMap())
    val drinkBreakdown: StateFlow<Map<String, Int>> = _drinkBreakdown.asStateFlow()

    private val _selectedRangeType = MutableStateFlow(HistoryRangeType.DAYS_7)
    val selectedRangeType: StateFlow<HistoryRangeType> = _selectedRangeType.asStateFlow()

    private val _startDate = MutableStateFlow<LocalDate>(LocalDate.now().minusDays(6))
    val startDate: StateFlow<LocalDate> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val endDate: StateFlow<LocalDate> = _endDate.asStateFlow()

    val useMetric: StateFlow<Boolean> = repository.userPreferencesFlow
        .map { it.useMetric }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    init {
        loadHistory()
    }

    fun setRangeType(type: HistoryRangeType) {
        _selectedRangeType.value = type
        when (type) {
            HistoryRangeType.DAYS_7 -> {
                _endDate.value = LocalDate.now()
                _startDate.value = LocalDate.now().minusDays(6)
                loadHistory()
            }
            HistoryRangeType.DAYS_30 -> {
                _endDate.value = LocalDate.now()
                _startDate.value = LocalDate.now().minusDays(29)
                loadHistory()
            }
            HistoryRangeType.CUSTOM -> {
                // Do not auto-load here until dates are picked
            }
        }
    }

    fun setCustomRange(start: LocalDate, end: LocalDate) {
        _startDate.value = start
        _endDate.value = end
        _selectedRangeType.value = HistoryRangeType.CUSTOM
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _historyData.value = repository.getHistoryBetween(_startDate.value, _endDate.value)
            _drinkBreakdown.value = repository.getDrinkBreakdownBetween(_startDate.value, _endDate.value)
        }
    }
}
