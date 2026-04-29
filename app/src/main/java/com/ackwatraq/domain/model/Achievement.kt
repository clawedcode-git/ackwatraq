package com.ackwatraq.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: AchievementType,
    val threshold: Int,
    val unlocked: Boolean = false,
    val unlockedAt: Long? = null
)

enum class AchievementType {
    STREAK, VOLUME_DAILY, VOLUME_TOTAL, CONSISTENCY
}
