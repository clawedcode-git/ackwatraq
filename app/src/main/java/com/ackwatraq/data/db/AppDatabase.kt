package com.ackwatraq.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ackwatraq.domain.model.Achievement
import com.ackwatraq.domain.model.IntakeRecord
import com.ackwatraq.domain.model.NotificationRecord
import com.ackwatraq.data.db.converter.DateTimeConverter
import com.ackwatraq.data.db.dao.IntakeDao
import com.ackwatraq.data.db.dao.AchievementDao
import com.ackwatraq.data.db.dao.NotificationDao

@Database(entities = [IntakeRecord::class, Achievement::class, NotificationRecord::class], version = 2, exportSchema = false)
@TypeConverters(DateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun intakeDao(): IntakeDao
    abstract fun achievementDao(): AchievementDao
    abstract fun notificationDao(): NotificationDao
}
