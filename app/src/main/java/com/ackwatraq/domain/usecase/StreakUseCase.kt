package com.ackwatraq.domain.usecase

import com.ackwatraq.data.db.dao.IntakeDao
import com.ackwatraq.domain.model.UserPreferences
import java.time.LocalDateTime

object StreakUseCase {
    suspend fun getCurrentStreak(intakeDao: IntakeDao, goalMl: Int): Int {
        var streak = 0
        var date = LocalDateTime.now().toLocalDate()
        while (true) {
            val start = date.atStartOfDay()
            val end = date.plusDays(1).atStartOfDay()
            val intake = intakeDao.getTotalIntakeBetween(start, end) ?: 0
            if (intake >= goalMl) {
                streak++
                date = date.minusDays(1)
            } else break
        }
        return streak
    }
}
