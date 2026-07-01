package com.ackwatraq.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        android.util.Log.d("ReminderWorker", "Executing doWork()")
        
        val app = context.applicationContext as com.ackwatraq.AckwatraqApplication
        val prefs = app.repository.getUserPreferences()
        
        if (!prefs.remindersEnabled) {
            android.util.Log.d("ReminderWorker", "Reminders are disabled in settings")
            return Result.success()
        }

        // 1. Quiet Hours (Silence Mode)
        val currentHour = java.time.LocalTime.now().hour
        if (isQuietHours(currentHour, prefs.quietHoursStart, prefs.quietHoursEnd)) {
            android.util.Log.d("ReminderWorker", "Silencing reminder during quiet hours ($currentHour)")
            return Result.success()
        }

        // 2. Logging Gap Detection (2 hours)
        val lastRecord = app.repository.getMostRecentRecord()
        if (lastRecord != null) {
            val twoHoursAgo = java.time.LocalDateTime.now().minusHours(2)
            if (lastRecord.timestamp.isAfter(twoHoursAgo)) {
                android.util.Log.d("ReminderWorker", "Skipping reminder; logged a drink recently (${lastRecord.timestamp})")
                return Result.success()
            }
        }

        // 3. Activity-aware Alerts (Step counter delta >= 1500)
        var isStepAlert = false
        try {
            val stepPrefs = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
            val todaySteps = stepPrefs.getInt("today_steps", 0)
            val lastAlertSteps = stepPrefs.getInt("last_alert_steps", 0)
            if (todaySteps - lastAlertSteps >= 1500) {
                isStepAlert = true
                stepPrefs.edit().putInt("last_alert_steps", todaySteps).apply()
            }
        } catch (e: Exception) {
            android.util.Log.e("ReminderWorker", "Failed to check step alert baseline", e)
        }

        val titleText = if (isStepAlert) "Active Day Reminder" else "Hydration Reminder"
        val messageText = if (isStepAlert) {
            "Active day! You've taken over 1,500 steps since your last drink. Time to rehydrate! 👣"
        } else {
            "It's been over 2 hours since your last drink! Time to hydrate. 💧"
        }

        try {
            app.repository.notificationDao.insert(com.ackwatraq.domain.model.NotificationRecord(message = messageText))
        } catch (e: Exception) {
            android.util.Log.e("ReminderWorker", "Failed to insert notification record", e)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            android.util.Log.e("ReminderWorker", "Missing POST_NOTIFICATIONS permission")
            return Result.success()
        }

        createNotificationChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titleText)
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)

        return Result.success()
    }

    private fun isQuietHours(currentHour: Int, startHour: Int, endHour: Int): Boolean {
        if (startHour == endHour) return false
        return if (startHour < endHour) {
            currentHour in startHour until endHour
        } else {
            currentHour >= startHour || currentHour < endHour
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminders"
            val descriptionText = "Periodic reminders to drink water"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "hydration_channel"
        const val NOTIFICATION_ID = 1001
    }
}
