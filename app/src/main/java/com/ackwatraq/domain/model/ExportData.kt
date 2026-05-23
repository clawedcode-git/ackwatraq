package com.ackwatraq.domain.model

data class ExportData(
    val preferences: UserPreferences,
    val intakeRecords: List<IntakeRecord>,
    val achievements: List<Achievement>,
    val notifications: List<NotificationRecord>
)
