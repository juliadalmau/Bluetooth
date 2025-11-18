package com.example.bluetooth.model.data

data class ElevationReading(
    val timestamp: Long,
    val elevationAngle: Float,
    val algorithmType: AlgorithmType
)

enum class AlgorithmType {
    ALGORITHM_1_ACCELEROMETER,
    ALGORITHM_2_SENSOR_FUSION
}