package com.ackwatraq

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.ackwatraq.data.db.AppDatabase
import com.ackwatraq.data.repository.WaterRepository
import com.ackwatraq.data.repository.StepRepository
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class AckwatraqApplication : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var repository: WaterRepository
        private set

    lateinit var stepRepository: StepRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        stepRepository = StepRepository(this)

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `message` TEXT NOT NULL, `isRead` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)")
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE intake_records ADD COLUMN drinkType TEXT NOT NULL DEFAULT 'Water'")
            }
        }

        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "ackwatraq-db"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()

        repository = WaterRepository(
            context = this,
            intakeDao = database.intakeDao(),
            achievementDao = database.achievementDao(),
            notificationDao = database.notificationDao(),
            dataStore = dataStore
        )

        kotlinx.coroutines.MainScope().launch {
            repository.userPreferencesFlow.collect { prefs ->
                val workManager = androidx.work.WorkManager.getInstance(this@AckwatraqApplication)
                if (prefs.remindersEnabled) {
                    val request = androidx.work.PeriodicWorkRequestBuilder<com.ackwatraq.worker.ReminderWorker>(2, java.util.concurrent.TimeUnit.HOURS).build()
                    workManager.enqueueUniquePeriodicWork(
                        "hydration_reminder",
                        androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                        request
                    )
                } else {
                    workManager.cancelUniqueWork("hydration_reminder")
                }
            }
        }
    }
}
