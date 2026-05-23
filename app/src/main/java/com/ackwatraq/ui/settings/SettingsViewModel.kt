package com.ackwatraq.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackwatraq.data.repository.WaterRepository
import com.ackwatraq.domain.model.AppTheme
import com.ackwatraq.domain.model.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: WaterRepository) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences()
            repository.saveUserPreferences(currentPrefs.copy(theme = theme))
        }
    }

    fun setDailyGoal(goalMl: Int) {
        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences()
            repository.saveUserPreferences(currentPrefs.copy(dailyGoalMl = goalMl))
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences()
            repository.saveUserPreferences(currentPrefs.copy(remindersEnabled = enabled))
        }
    }

    fun setUseMetric(metric: Boolean) {
        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences()
            repository.saveUserPreferences(currentPrefs.copy(useMetric = metric))
        }
    }

    fun setNickname(nickname: String) {
        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences()
            repository.saveUserPreferences(currentPrefs.copy(nickname = nickname))
        }
    }

    private val gson = com.google.gson.GsonBuilder()
        .registerTypeAdapter(java.time.LocalDateTime::class.java, object : com.google.gson.JsonSerializer<java.time.LocalDateTime>, com.google.gson.JsonDeserializer<java.time.LocalDateTime> {
            override fun serialize(src: java.time.LocalDateTime, typeOfSrc: java.lang.reflect.Type, context: com.google.gson.JsonSerializationContext): com.google.gson.JsonElement {
                return com.google.gson.JsonPrimitive(src.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            }
            override fun deserialize(json: com.google.gson.JsonElement, typeOfT: java.lang.reflect.Type, context: com.google.gson.JsonDeserializationContext): java.time.LocalDateTime {
                return java.time.LocalDateTime.parse(json.asString, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        })
        .create()

    fun exportData(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val prefs = repository.getUserPreferences()
                val intakes = repository.getAllIntakeRecords()
                val achievements = repository.getAllAchievements()
                val notifications = repository.getAllNotifications()

                val exportData = com.ackwatraq.domain.model.ExportData(prefs, intakes, achievements, notifications)
                val jsonString = gson.toJson(exportData)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importData(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    val exportData = gson.fromJson(jsonString, com.ackwatraq.domain.model.ExportData::class.java)

                    repository.restoreData(exportData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
