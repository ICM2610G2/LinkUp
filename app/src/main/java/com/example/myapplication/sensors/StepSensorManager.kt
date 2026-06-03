package com.example.myapplication.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepSensorManager(context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // TYPE_STEP_COUNTER: pasos acumulados desde el último reinicio del dispositivo
    private val stepCounterSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    // TYPE_STEP_DETECTOR: dispara un evento por cada paso detectado
    private val stepDetectorSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val _steps = MutableStateFlow(0)
    val steps = _steps.asStateFlow()

    private val _isMoving = MutableStateFlow(false)
    val isMoving = _isMoving.asStateFlow()

    val isAvailable: Boolean get() = stepCounterSensor != null || stepDetectorSensor != null

    // Referencia base: primer valor del sensor al iniciar la sesión
    private var baseStepCount: Int? = null

    // Timer para detectar que el usuario dejó de moverse (2 segundos sin pasos)
    private var lastStepTime = 0L
    private val MOVING_TIMEOUT_MS = 2000L

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {

                Sensor.TYPE_STEP_COUNTER -> {
                    val total = event.values[0].toInt()
                    if (baseStepCount == null) baseStepCount = total
                    _steps.value = total - (baseStepCount ?: total)
                    markMoving()
                }

                Sensor.TYPE_STEP_DETECTOR -> {
                    // Fallback si no hay STEP_COUNTER: suma 1 por evento
                    if (stepCounterSensor == null) {
                        _steps.value += 1
                    }
                    markMoving()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun markMoving() {
        lastStepTime = System.currentTimeMillis()
        _isMoving.value = true

        // Hilo simple para apagar isMoving tras timeout
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (System.currentTimeMillis() - lastStepTime >= MOVING_TIMEOUT_MS) {
                _isMoving.value = false
            }
        }, MOVING_TIMEOUT_MS)
    }

    fun start() {
        stepCounterSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        // Solo registrar detector si no hay counter (evitar doble conteo)
        if (stepCounterSensor == null) {
            stepDetectorSensor?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_FASTEST)
            }
        }
    }

    fun stop() {
        sensorManager.unregisterListener(listener)
    }

    fun reset() {
        baseStepCount = null
        _steps.value = 0
        _isMoving.value = false
    }
}