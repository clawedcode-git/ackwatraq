package com.ackwatraq.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackwatraq.data.repository.WaterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.ackwatraq.data.repository.StepRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class HomeViewModel(
    private val repository: WaterRepository,
    private val stepRepository: StepRepository
) : ViewModel() {
    private val _intake = MutableStateFlow(0)
    val intake: StateFlow<Int> = _intake.asStateFlow()

    val currentSteps: StateFlow<Int> = stepRepository.currentSteps

    fun startStepTracking() {
        stepRepository.startListening()
    }

    fun stopStepTracking() {
        stepRepository.stopListening()
    }

    val dailyGoal: StateFlow<Int> = repository.userPreferencesFlow
        .map { it.dailyGoalMl }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 2000
        )

    val useMetric: StateFlow<Boolean> = repository.userPreferencesFlow
        .map { it.useMetric }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val nickname: StateFlow<String> = repository.userPreferencesFlow
        .map { it.nickname }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    init {
        loadTodayIntake()
    }

    private fun loadTodayIntake() {
        viewModelScope.launch {
            _intake.value = repository.getTodayIntake()
        }
    }

    fun addWater(amount: Int) {
        viewModelScope.launch {
            repository.logIntake(amount)
            _intake.value = repository.getTodayIntake()
        }
    }

    fun editTotalIntake(newTotal: Int) {
        viewModelScope.launch {
            val currentTotal = repository.getTodayIntake()
            val difference = newTotal - currentTotal
            if (difference != 0) {
                repository.logIntake(difference)
                _intake.value = repository.getTodayIntake()
            }
        }
    }

    val notifications: StateFlow<List<AppNotification>> = repository.notificationDao.getAllNotifications()
        .map { records ->
            records.map { AppNotification(it.id, it.message, it.isRead) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            repository.notificationDao.markAsRead(id)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.notificationDao.clearAll()
        }
    }
}

data class AppNotification(val id: Int, val message: String, val isRead: Boolean = false)
