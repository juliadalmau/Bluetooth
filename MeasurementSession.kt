package com.example.bluetooth.model.data

import java.util.UUID

data class MeasurementSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val startTime: Long,
    val endTime: Long? = null,
    val sensorType: SensorType,
    val duration: Long = 0L,
    val algorithm1Results: List<ElevationReading> = emptyList(),
    val algorithm2Results: List<ElevationReading> = emptyList(),
    val rawSensorData: List<SensorData> = emptyList()
)