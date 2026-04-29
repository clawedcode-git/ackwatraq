package com.ackwatraq.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "intake_records")
data class IntakeRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
