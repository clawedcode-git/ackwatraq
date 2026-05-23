package com.ackwatraq.data.db.dao

import androidx.room.*
import com.ackwatraq.domain.model.Achievement

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    suspend fun getAll(): List<Achievement>

    @Query("SELECT * FROM achievements WHERE type = :type")
    suspend fun getByType(type: com.ackwatraq.domain.model.AchievementType): List<Achievement>

    @Update
    suspend fun update(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<Achievement>)

    @Query("DELETE FROM achievements")
    suspend fun clearAll()
}
