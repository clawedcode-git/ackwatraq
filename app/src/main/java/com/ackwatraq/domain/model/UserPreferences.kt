package com.ackwatraq.domain.model

data class UserPreferences(
    val dailyGoalMl: Int = 2000,
    val weightKg: Float = 70f,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val reminderIntervalMinutes: Int = 60,
    val remindersEnabled: Boolean = true,
    val useMetric: Boolean = true
)

enum class ActivityLevel {
    SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE
}
