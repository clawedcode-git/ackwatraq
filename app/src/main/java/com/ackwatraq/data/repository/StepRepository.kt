package com.ackwatraq.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class StepRepository(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    
    private val prefs = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)

    private val _currentSteps = MutableStateFlow(0)
    val currentSteps: StateFlow<Int> = _currentSteps

    fun startListening() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            val todayStr = LocalDate.now().toString()
            val savedDate = prefs.getString("last_date", "")
            
            var baseline = prefs.getInt("baseline_steps", -1)
            
            if (savedDate != todayStr || baseline == -1 || totalSteps < baseline) {
                baseline = totalSteps
                prefs.edit()
                    .putString("last_date", todayStr)
                    .putInt("baseline_steps", baseline)
                    .apply()
            }
            
            val todaySteps = totalSteps - baseline
            _currentSteps.value = todaySteps
            prefs.edit().putInt("today_steps", todaySteps).apply()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
