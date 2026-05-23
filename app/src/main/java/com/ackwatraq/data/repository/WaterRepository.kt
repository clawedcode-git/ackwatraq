package com.ackwatraq.data.repository

import com.ackwatraq.data.db.dao.AchievementDao
import com.ackwatraq.data.db.dao.IntakeDao
import com.ackwatraq.domain.model.Achievement
import com.ackwatraq.domain.model.IntakeRecord
import com.ackwatraq.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit
import java.time.LocalDateTime

import com.ackwatraq.data.db.dao.NotificationDao
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ackwatraq.worker.ReminderWorker
import java.util.concurrent.TimeUnit

class WaterRepository(
    private val context: android.content.Context,
    private val intakeDao: IntakeDao,
    private val achievementDao: AchievementDao,
    val notificationDao: NotificationDao,
    private val dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
) {
    suspend fun logIntake(amountMl: Int) {
        intakeDao.insert(IntakeRecord(amountMl = amountMl))
        checkAchievements()
        
        val prefs = getUserPreferences()
        if (prefs.remindersEnabled) {
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(2, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "inactivity_reminder",
                ExistingWorkPolicy.REPLACE,
                request
            )
        } else {
            WorkManager.getInstance(context).cancelUniqueWork("inactivity_reminder")
        }
    }

    suspend fun getTodayIntake(): Int {
        val start = LocalDateTime.now().toLocalDate().atStartOfDay()
        val end = start.plusDays(1)
        return intakeDao.getTotalIntakeBetween(start, end) ?: 0
    }

    suspend fun getRecordsForDate(date: LocalDateTime): List<IntakeRecord> {
        val start = date.toLocalDate().atStartOfDay()
        val end = start.plusDays(1)
        return intakeDao.getRecordsBetween(start, end)
    }

    suspend fun getHistoryBetween(start: java.time.LocalDate, end: java.time.LocalDate): Map<java.time.LocalDate, Int> {
        val startDateTime = start.atStartOfDay()
        val endDateTime = end.plusDays(1).atStartOfDay()
        
        val records = intakeDao.getRecordsBetween(startDateTime, endDateTime)
        
        val historyData = mutableMapOf<java.time.LocalDate, Int>()
        
        // Pad all days in range with 0
        var current = start
        while (!current.isAfter(end)) {
            historyData[current] = 0
            current = current.plusDays(1)
        }
        
        for (record in records) {
            val date = record.timestamp.toLocalDate()
            if (historyData.containsKey(date)) {
                historyData[date] = (historyData[date] ?: 0) + record.amountMl
            }
        }
        return historyData
    }

    suspend fun getAchievements(): List<Achievement> {
        val defaults = listOf(
            Achievement(
                id = "first_sip",
                title = "First Sip",
                description = "Log your first water intake.",
                type = com.ackwatraq.domain.model.AchievementType.VOLUME_DAILY,
                threshold = 250
            ),
            Achievement(
                id = "2l_club",
                title = "2L Club",
                description = "Drink 2000mL in a single day.",
                type = com.ackwatraq.domain.model.AchievementType.VOLUME_DAILY,
                threshold = 2000
            ),
            Achievement(
                id = "camel",
                title = "Camel",
                description = "Drink 3000mL in a single day.",
                type = com.ackwatraq.domain.model.AchievementType.VOLUME_DAILY,
                threshold = 3000
            ),
            Achievement(
                id = "streak_3",
                title = "3-Day Streak",
                description = "Meet your daily goal for 3 consecutive days.",
                type = com.ackwatraq.domain.model.AchievementType.STREAK,
                threshold = 3
            ),
            Achievement(
                id = "streak_7",
                title = "Hydration Master",
                description = "Meet your daily goal for a full week.",
                type = com.ackwatraq.domain.model.AchievementType.STREAK,
                threshold = 7
            ),
            Achievement(
                id = "volume_total_10l",
                title = "Ocean Drinker",
                description = "Log 10,000mL of water in total.",
                type = com.ackwatraq.domain.model.AchievementType.VOLUME_TOTAL,
                threshold = 10000
            )
        )
        
        var current = achievementDao.getAll()
        val currentIds = current.map { it.id }.toSet()
        val missing = defaults.filter { it.id !in currentIds }
        
        if (missing.isNotEmpty()) {
            achievementDao.insertAll(missing)
            checkAchievements()
            current = achievementDao.getAll()
        }
        
        return current
    }

    private suspend fun checkAchievements() {
        val achievements = achievementDao.getAll()
        val todayIntake = getTodayIntake()
        val prefs = getUserPreferences()
        achievements.forEach { ach ->
            if (!ach.unlocked && shouldUnlock(ach, todayIntake, prefs)) {
                achievementDao.update(ach.copy(unlocked = true, unlockedAt = System.currentTimeMillis()))
            }
        }
    }

    private suspend fun shouldUnlock(ach: Achievement, todayIntake: Int, prefs: UserPreferences): Boolean {
        return when (ach.type) {
            com.ackwatraq.domain.model.AchievementType.VOLUME_DAILY -> todayIntake >= ach.threshold
            com.ackwatraq.domain.model.AchievementType.VOLUME_TOTAL -> {
                val start = java.time.LocalDateTime.of(2000, 1, 1, 0, 0)
                val end = java.time.LocalDateTime.now().plusDays(1)
                val totalVolume = intakeDao.getTotalIntakeBetween(start, end) ?: 0
                totalVolume >= ach.threshold
            }
            com.ackwatraq.domain.model.AchievementType.STREAK -> {
                var currentStreak = 0
                var date = java.time.LocalDate.now()
                val todayGoalMet = todayIntake >= prefs.dailyGoalMl
                
                // If today is met, count it!
                if (todayGoalMet) {
                    currentStreak++
                }
                
                // Now check backwards from yesterday
                date = date.minusDays(1)
                while (true) {
                    val start = date.atStartOfDay()
                    val end = start.plusDays(1)
                    val dailySum = intakeDao.getTotalIntakeBetween(start, end) ?: 0
                    if (dailySum >= prefs.dailyGoalMl) {
                        currentStreak++
                        date = date.minusDays(1)
                    } else {
                        break
                    }
                }
                currentStreak >= ach.threshold
            }
            else -> false
        }
    }

    private val THEME_KEY = androidx.datastore.preferences.core.stringPreferencesKey("theme")
    private val DAILY_GOAL_KEY = androidx.datastore.preferences.core.intPreferencesKey("daily_goal_ml")
    private val REMINDERS_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("reminders_enabled")
    private val METRIC_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("use_metric")
    private val NICKNAME_KEY = androidx.datastore.preferences.core.stringPreferencesKey("nickname")

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { prefs ->
        val themeStr = prefs[THEME_KEY] ?: com.ackwatraq.domain.model.AppTheme.SYSTEM.name
        val dailyGoal = prefs[DAILY_GOAL_KEY] ?: 2000
        val reminders = prefs[REMINDERS_KEY] ?: true
        val metric = prefs[METRIC_KEY] ?: true
        val nick = prefs[NICKNAME_KEY] ?: ""
        
        UserPreferences(
            theme = com.ackwatraq.domain.model.AppTheme.valueOf(themeStr),
            dailyGoalMl = dailyGoal,
            remindersEnabled = reminders,
            useMetric = metric,
            nickname = nick
        )
    }

    suspend fun getLifetimeVolume(): Int {
        val start = java.time.LocalDateTime.of(2000, 1, 1, 0, 0)
        val end = java.time.LocalDateTime.now().plusDays(1)
        return intakeDao.getTotalIntakeBetween(start, end) ?: 0
    }

    suspend fun getCurrentStreak(): Int {
        val prefs = getUserPreferences()
        val todayIntake = getTodayIntake()
        
        var currentStreak = 0
        var date = java.time.LocalDate.now()
        val todayGoalMet = todayIntake >= prefs.dailyGoalMl
        
        if (todayGoalMet) {
            currentStreak++
        }
        
        date = date.minusDays(1)
        while (true) {
            val start = date.atStartOfDay()
            val end = start.plusDays(1)
            val dailySum = intakeDao.getTotalIntakeBetween(start, end) ?: 0
            if (dailySum >= prefs.dailyGoalMl) {
                currentStreak++
                date = date.minusDays(1)
            } else {
                break
            }
        }
        return currentStreak
    }

    suspend fun getUserPreferences(): UserPreferences {
        return userPreferencesFlow.first()
    }

    suspend fun saveUserPreferences(prefs: UserPreferences) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = prefs.theme.name
            preferences[DAILY_GOAL_KEY] = prefs.dailyGoalMl
            preferences[REMINDERS_KEY] = prefs.remindersEnabled
            preferences[METRIC_KEY] = prefs.useMetric
            preferences[NICKNAME_KEY] = prefs.nickname
        }
    }

    suspend fun getAllIntakeRecords(): List<IntakeRecord> = intakeDao.getAllRecords()
    suspend fun getAllAchievements(): List<Achievement> = achievementDao.getAll()
    suspend fun getAllNotifications(): List<com.ackwatraq.domain.model.NotificationRecord> = notificationDao.getAll()

    suspend fun restoreData(exportData: com.ackwatraq.domain.model.ExportData) {
        // Clear all
        intakeDao.clearAll()
        achievementDao.clearAll()
        notificationDao.clearAll()

        // Insert all
        intakeDao.insertAll(exportData.intakeRecords)
        achievementDao.insertAll(exportData.achievements)
        notificationDao.insertAll(exportData.notifications)

        // Save preferences
        saveUserPreferences(exportData.preferences)
    }
}
