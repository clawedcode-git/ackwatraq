package com.ackwatraq.data.repository

import com.ackwatraq.data.db.dao.AchievementDao
import com.ackwatraq.data.db.dao.IntakeDao
import com.ackwatraq.domain.model.Achievement
import com.ackwatraq.domain.model.IntakeRecord
import com.ackwatraq.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class WaterRepository(
    private val intakeDao: IntakeDao,
    private val achievementDao: AchievementDao,
    private val dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
) {
    suspend fun logIntake(amountMl: Int) {
        intakeDao.insert(IntakeRecord(amountMl = amountMl))
        checkAchievements()
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

    private fun shouldUnlock(ach: Achievement, todayIntake: Int, prefs: UserPreferences): Boolean {
        return when (ach.type) {
            com.ackwatraq.domain.model.AchievementType.VOLUME_DAILY -> todayIntake >= ach.threshold
            else -> false
        }
    }

    suspend fun getUserPreferences(): UserPreferences {
        return UserPreferences()
    }

    suspend fun saveUserPreferences(prefs: UserPreferences) {}
}
