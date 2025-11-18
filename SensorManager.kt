package com.example.bluetooth.model.sensors

import com.example.bluetooth.model.data.SensorData
import com.example.bluetooth.model.data.SensorType
/*
interface SensorManager {
    fun isAvailable(): Boolean
    fun connect(onConnected: () -> Unit, onError: (String) -> Unit)
    fun disconnect()
    fun startListening(callback: (SensorData) -> Unit)
    fun stopListening()
    fun getSensorType(): SensorType
}*/

interface SensorManager {
    fun isAvailable(): Boolean
    fun connect(onConnected: () -> Unit, onError: (String) -> Unit)
    fun disconnect()
    fun startListening(callback: (SensorData) -> Unit)
    fun stopListening()
    fun getSensorType(): SensorType
}