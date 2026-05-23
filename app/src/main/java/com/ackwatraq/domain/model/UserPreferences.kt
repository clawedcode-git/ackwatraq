package com.ackwatraq.domain.model

data class UserPreferences(
    val dailyGoalMl: Int = 2000,
    val weightKg: Float = 70f,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val reminderIntervalMinutes: Int = 60,
    val remindersEnabled: Boolean = true,
    val useMetric: Boolean = true,
    val theme: AppTheme = AppTheme.SYSTEM,
    val nickname: String = ""
)

enum class ActivityLevel {
    SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE
}

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}
