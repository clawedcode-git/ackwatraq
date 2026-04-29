package com.ackwatraq.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ackwatraq.domain.model.Achievement
import com.ackwatraq.domain.model.IntakeRecord
import com.ackwatraq.data.db.converter.DateTimeConverter
import com.ackwatraq.data.db.dao.IntakeDao
import com.ackwatraq.data.db.dao.AchievementDao

@Database(entities = [IntakeRecord::class, Achievement::class], version = 1)
@TypeConverters(DateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun intakeDao(): IntakeDao
    abstract fun achievementDao(): AchievementDao
}
