package com.ackwatraq.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackwatraq.data.repository.WaterRepository
import com.ackwatraq.domain.model.AppTheme
import com.ackwatraq.domain.model.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: WaterRepository) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences()
            repository.saveUserPreferences(currentPrefs.copy(theme = theme))
        }
    }

    fun setDailyGoal(goalMl: Int) {
        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences()
            repository.saveUserPreferences(currentPrefs.copy(dailyGoalMl = goalMl))
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences()
            repository.saveUserPreferences(currentPrefs.copy(remindersEnabled = enabled))
        }
    }

    fun setUseMetric(metric: Boolean) {
        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences()
            repository.saveUserPreferences(currentPrefs.copy(useMetric = metric))
        }
    }

    fun setNickname(nickname: String) {
        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences()
            repository.saveUserPreferences(currentPrefs.copy(nickname = nickname))
        }
    }
}
