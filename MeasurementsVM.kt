package com.example.bluetooth.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.model.algorithms.AccelerometerAlgorithm
import com.example.bluetooth.model.algorithms.SensorFusionAlgorithm
import com.example.bluetooth.model.data.AlgorithmType
import com.example.bluetooth.model.data.ElevationReading
import com.example.bluetooth.model.data.MeasurementSession
import com.example.bluetooth.model.data.SensorData
import com.example.bluetooth.model.export.CsvExporter
import com.example.bluetooth.model.repository.MeasurementRepository
import com.example.bluetooth.model.sensors.SensorManager
import kotlinx.coroutines.launch

class MeasurementViewModel(
    private val repository: MeasurementRepository,
    private val sensorManager: SensorManager
) : ViewModel() {

    private val _isRecording = MutableLiveData<Boolean>(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _algorithm1Elevation = MutableLiveData<Float>()
    val algorithm1Elevation: LiveData<Float> = _algorithm1Elevation

    private val _algorithm2Elevation = MutableLiveData<Float>()
    val algorithm2Elevation: LiveData<Float> = _algorithm2Elevation

    // For real-time graph
    private val _algorithm1History = MutableLiveData<List<ElevationReading>>()
    val algorithm1History: LiveData<List<ElevationReading>> = _algorithm1History

    private val _algorithm2History = MutableLiveData<List<ElevationReading>>()
    val algorithm2History: LiveData<List<ElevationReading>> = _algorithm2History

    private val _exportStatus = MutableLiveData<ExportStatus>()
    val exportStatus: LiveData<ExportStatus> = _exportStatus

    private val algorithm1 = AccelerometerAlgorithm(alpha = 0.2f)
    private val algorithm2 = SensorFusionAlgorithm(alpha = 0.98f)

    private var currentSession: MeasurementSession? = null
    private val algorithm1ResultsList = mutableListOf<ElevationReading>()
    private val algorithm2ResultsList = mutableListOf<ElevationReading>()
    private val rawDataList = mutableListOf<SensorData>()

    private var startTime: Long = 0L

    fun startMeasurement() {
        _isRecording.value = true
        startTime = System.currentTimeMillis()

        // Reset algorithms
        algorithm1.reset()
        algorithm2.reset()

        // Clear previous data
        algorithm1ResultsList.clear()
        algorithm2ResultsList.clear()
        rawDataList.clear()

        // Create new session
        currentSession = MeasurementSession(
            startTime = startTime,
            sensorType = sensorManager.getSensorType()
        )

        // Start listening to sensor data
        sensorManager.startListening { sensorData ->
            processSensorData(sensorData)
        }
    }

    fun stopMeasurement() {
        _isRecording.value = false
        sensorManager.stopListening()

        val endTime = System.currentTimeMillis()

        // Finalize session
        currentSession = currentSession?.copy(
            endTime = endTime,
            duration = endTime - startTime,
            algorithm1Results = algorithm1ResultsList.toList(),
            algorithm2Results = algorithm2ResultsList.toList(),
            rawSensorData = rawDataList.toList()
        )

        // Save to database
        currentSession?.let { session ->
            viewModelScope.launch {
                repository.saveMeasurement(session)
            }
        }
    }

    private fun processSensorData(sensorData: SensorData) {
        // Store raw data
        rawDataList.add(sensorData)

        // Calculate elevation with Algorithm 1
        val elevation1 = algorithm1.calculateElevation(sensorData)
        val reading1 = ElevationReading(
            timestamp = sensorData.timestamp,
            elevationAngle = elevation1,
            algorithmType = AlgorithmType.ALGORITHM_1_ACCELEROMETER
        )
        algorithm1ResultsList.add(reading1)
        _algorithm1Elevation.postValue(elevation1)

        // Calculate elevation with Algorithm 2
        val elevation2 = algorithm2.calculateElevation(sensorData)
        val reading2 = ElevationReading(
            timestamp = sensorData.timestamp,
            elevationAngle = elevation2,
            algorithmType = AlgorithmType.ALGORITHM_2_SENSOR_FUSION
        )
        algorithm2ResultsList.add(reading2)
        _algorithm2Elevation.postValue(elevation2)

        // Update history for graph (keep last 1000 points to avoid memory issues)
        _algorithm1History.postValue(algorithm1ResultsList.takeLast(1000))
        _algorithm2History.postValue(algorithm2ResultsList.takeLast(1000))
    }

    fun exportCurrentSession() {
        currentSession?.let { session ->
            viewModelScope.launch {
                try {
                    val uri = CsvExporter.exportToCSV(session)
                    _exportStatus.value = ExportStatus.Success(uri)
                } catch (e: Exception) {
                    _exportStatus.value = ExportStatus.Error(e.message ?: "Export failed")
                }
            }
        }
    }
}

private fun SensorManager.startListening(function: Any) {}

sealed class ExportStatus {
    data class Success(val uri: Uri) : ExportStatus()
    data class Error(val message: String) : ExportStatus()
}