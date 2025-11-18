package com.example.bluetooth.model.algorithms

import com.example.bluetooth.model.data.SensorData

interface ElevationCalculator {
    fun calculateElevation(sensorData: SensorData): Float
    fun reset()
}