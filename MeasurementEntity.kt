package com.example.bluetooth.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey val sessionId: String,
    val startTime: Long,
    val endTime: Long,
    val sensorType: String,
    val duration: Long,
    val algorithm1Data: String, // JSON string
    val algorithm2Data: String, // JSON string
    val rawData: String // JSON string (optional)
)