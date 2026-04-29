package com.ackwatraq.domain.usecase

import com.ackwatraq.domain.model.ActivityLevel
import com.ackwatraq.domain.model.UserPreferences

object CalculateGoalUseCase {
    fun execute(prefs: UserPreferences): Int {
        val base = prefs.weightKg * 30
        val activityMultiplier = when (prefs.activityLevel) {
            ActivityLevel.SEDENTARY -> 1.0f
            ActivityLevel.LIGHT -> 1.1f
            ActivityLevel.MODERATE -> 1.2f
            ActivityLevel.ACTIVE -> 1.3f
            ActivityLevel.VERY_ACTIVE -> 1.5f
        }
        return (base * activityMultiplier).toInt()
    }
}
