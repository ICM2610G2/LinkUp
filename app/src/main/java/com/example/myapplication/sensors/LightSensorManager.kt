package com.example.myapplication.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Umbral en lux: por debajo de este valor se considera "oscuro"
// Referencia: habitación con poca luz ~50 lux, noche ~10 lux
private const val DARK_THRESHOLD_LUX = 30f

class LightSensorManager(context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _isDark = MutableStateFlow(false)
    val isDark = _isDark.asStateFlow()

    // true si el dispositivo tiene sensor de luz
    val isAvailable: Boolean get() = lightSensor != null

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val lux = event.values[0]
            _isDark.value = lux < DARK_THRESHOLD_LUX
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun start() {
        lightSensor?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(listener)
    }
}