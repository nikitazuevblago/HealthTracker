package com.example.healthtracker

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepTracker(private val context: Context) : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var stepCount: Int = 0

    init {
        setupSensor()
    }

    private fun setupSensor() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    fun startCounting() {
        stepCounterSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopCounting() {
        sensorManager?.unregisterListener(this)
    }

    fun getStepCount(): Int = stepCount

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                stepCount = it.values[0].toInt()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this implementation
    }
}