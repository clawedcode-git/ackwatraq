package com.ackwatraq.data.db.dao

import androidx.room.*
import com.ackwatraq.domain.model.IntakeRecord
import java.time.LocalDateTime

@Dao
interface IntakeDao {
    @Insert
    suspend fun insert(record: IntakeRecord): Long

    @Query("SELECT * FROM intake_records WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp DESC")
    suspend fun getRecordsBetween(start: LocalDateTime, end: LocalDateTime): List<IntakeRecord>

    @Query("SELECT SUM(amountMl) FROM intake_records WHERE timestamp >= :start AND timestamp < :end")
    suspend fun getTotalIntakeBetween(start: LocalDateTime, end: LocalDateTime): Int?

    @Query("SELECT * FROM intake_records")
    suspend fun getAllRecords(): List<IntakeRecord>

    @Query("DELETE FROM intake_records")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<IntakeRecord>)
}
