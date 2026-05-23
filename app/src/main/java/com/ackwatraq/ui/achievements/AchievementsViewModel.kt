package com.ackwatraq.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackwatraq.data.repository.WaterRepository
import com.ackwatraq.domain.model.Achievement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AchievementsViewModel(private val repository: WaterRepository) : ViewModel() {
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _lifetimeVolume = MutableStateFlow(0)
    val lifetimeVolume: StateFlow<Int> = _lifetimeVolume.asStateFlow()

    init {
        loadAchievements()
    }

    fun loadAchievements() {
        viewModelScope.launch {
            _achievements.value = repository.getAchievements()
            _currentStreak.value = repository.getCurrentStreak()
            _lifetimeVolume.value = repository.getLifetimeVolume()
        }
    }
}
