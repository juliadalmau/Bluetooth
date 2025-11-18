package com.example.bluetooth.model.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.example.bluetooth.model.data.SensorData
import com.example.bluetooth.model.data.SensorType

class InternalSensorManager(private val context: Context) : SensorManager {
    private val systemSensorManager: android.hardware.SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager

    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private var accelerometerData: FloatArray = FloatArray(3)
    private var gyroscopeData: FloatArray = FloatArray(3)

    private var dataCallback: ((SensorData) -> Unit)? = null

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                when (it.sensor.type) {
                    Sensor.TYPE_LINEAR_ACCELERATION -> {
                        accelerometerData = it.values.clone()
                        emitSensorData()
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        gyroscopeData = it.values.clone()
                        emitSensorData()
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun isAvailable(): Boolean {
        return systemSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null
    }

    override fun connect(onConnected: () -> Unit, onError: (String) -> Unit) {
        accelerometer = systemSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        gyroscope = systemSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        if (accelerometer == null) {
            onError("Accelerometer not available")
            return
        }

        onConnected()
    }

    override fun disconnect() {
        stopListening()
    }

    override fun startListening(callback: (SensorData) -> Unit) {
        dataCallback = callback

        accelerometer?.let {
            systemSensorManager.registerListener(
                sensorEventListener,
                it,
                android.hardware.SensorManager.SENSOR_DELAY_GAME  // ✅ Use android.hardware.SensorManager
            )
        }

        gyroscope?.let {
            systemSensorManager.registerListener(
                sensorEventListener,
                it,
                android.hardware.SensorManager.SENSOR_DELAY_GAME  // ✅ Use android.hardware.SensorManager
            )
        }
    }

    override fun stopListening() {
        systemSensorManager.unregisterListener(sensorEventListener)
        dataCallback = null
    }

    override fun getSensorType(): SensorType = SensorType.INTERNAL

    private fun emitSensorData() {
        dataCallback?.invoke(
            SensorData(
                timestamp = System.nanoTime(),
                accelerometerX = accelerometerData[0],
                accelerometerY = accelerometerData[1],
                accelerometerZ = accelerometerData[2],
                gyroscopeX = gyroscopeData.getOrNull(0) ?: 0f,
                gyroscopeY = gyroscopeData.getOrNull(1) ?: 0f,
                gyroscopeZ = gyroscopeData.getOrNull(2) ?: 0f,
                sensorType = SensorType.INTERNAL
            )
        )
    }
}