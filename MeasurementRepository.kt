package com.example.bluetooth.model.repository

import com.example.bluetooth.model.data.ElevationReading
import com.example.bluetooth.model.data.MeasurementSession
import com.example.bluetooth.model.data.SensorData
import com.example.bluetooth.model.data.SensorType
import com.example.bluetooth.model.database.MeasurementDao
import com.example.bluetooth.model.database.MeasurementEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MeasurementRepository(private val measurementDao: MeasurementDao) {
    val allMeasurements: Flow<List<MeasurementSession>> =
        measurementDao.getAllMeasurements().map { entities ->
            entities.map { it.toMeasurementSession() }
        }

    suspend fun saveMeasurement(session: MeasurementSession) {
        measurementDao.insertMeasurement(session.toEntity())
    }

    suspend fun getMeasurementById(id: String): MeasurementSession? {
        return measurementDao.getMeasurementById(id)?.toMeasurementSession()
    }

    suspend fun deleteMeasurement(sessionId: String) {
        measurementDao.deleteMeasurementById(sessionId)
    }
}

// Extension functions for conversion
fun MeasurementSession.toEntity(): MeasurementEntity {
    val gson = Gson()
    return MeasurementEntity(
        sessionId = sessionId,
        startTime = startTime,
        endTime = endTime ?: System.currentTimeMillis(),
        sensorType = sensorType.name,
        duration = duration,
        algorithm1Data = gson.toJson(algorithm1Results),
        algorithm2Data = gson.toJson(algorithm2Results),
        rawData = gson.toJson(rawSensorData)
    )
}

fun MeasurementEntity.toMeasurementSession(): MeasurementSession {
    val gson = Gson()
    val type = object : TypeToken<List<ElevationReading>>() {}.type
    val rawType = object : TypeToken<List<SensorData>>() {}.type

    return MeasurementSession(
        sessionId = sessionId,
        startTime = startTime,
        endTime = endTime,
        sensorType = SensorType.valueOf(sensorType),
        duration = duration,
        algorithm1Results = gson.fromJson(algorithm1Data, type),
        algorithm2Results = gson.fromJson(algorithm2Data, type),
        rawSensorData = gson.fromJson(rawData, rawType)
    )
}