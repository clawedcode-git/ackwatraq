package com.ackwatraq.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
