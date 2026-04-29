package com.ackwatraq.data.store

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager(
    private val dataStore: androidx.datastore.core.DataStore<Preferences>
) {
    companion object {
        val DAILY_GOAL_KEY = intPreferencesKey("daily_goal_ml")
    }

    val dailyGoalFlow: Flow<Int> = dataStore.data.map { it[DAILY_GOAL_KEY] ?: 2000 }

    suspend fun setDailyGoal(goalMl: Int) {
        dataStore.edit { it[DAILY_GOAL_KEY] = goalMl }
    }
}
