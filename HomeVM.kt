package com.example.bluetooth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bluetooth.model.data.SensorType
import com.example.bluetooth.model.sensors.InternalSensorManager
import com.example.bluetooth.model.sensors.PolarSensorManager
import com.example.bluetooth.model.sensors.SensorManager



class HomeViewModel(
    private val application: Application
) : AndroidViewModel(application) {

    private val _selectedSensorType = MutableLiveData<SensorType>(SensorType.INTERNAL)
    val selectedSensorType: LiveData<SensorType> = _selectedSensorType

    private val _connectionStatus = MutableLiveData<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: LiveData<ConnectionStatus> = _connectionStatus

    private val _availablePolarDevices = MutableLiveData<List<PolarDevice>>()
    val availablePolarDevices: LiveData<List<PolarDevice>> = _availablePolarDevices

    private val _isScanning = MutableLiveData<Boolean>(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private var currentSensorManager: SensorManager? = null
    private var polarSensorManager: PolarSensorManager? = null

    fun selectSensorType(sensorType: SensorType) {
        _selectedSensorType.value = sensorType

        // If switching to Polar, start scanning automatically
        if (sensorType == SensorType.POLAR_VERITY_SENSE) {
            scanForPolarDevices()
        }
    }

    /**
     * Scan for available Polar devices
     */
    fun scanForPolarDevices() {
        _isScanning.value = true

        if (polarSensorManager == null) {
            polarSensorManager = PolarSensorManager(application)
        }

        val deviceList = mutableListOf<PolarDevice>()

        polarSensorManager?.scanForDevices(
            onDeviceFound = { deviceId, deviceName ->
                deviceList.add(PolarDevice(deviceId, deviceName))
                _availablePolarDevices.value = deviceList.toList()
            },
            onError = { error ->
                _connectionStatus.value = ConnectionStatus.Error("Scan error: $error")
                _isScanning.value = false
            }
        )

        // Stop scanning after 10 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _isScanning.value = false
        }, 10000)
    }

    /**
     * Connect to internal phone sensors
     */
    fun connectToInternalSensor() {
        _connectionStatus.value = ConnectionStatus.Connecting

        currentSensorManager = InternalSensorManager(application)

        currentSensorManager?.connect(
            onConnected = {
                _connectionStatus.value = ConnectionStatus.Connected
            },
            onError = { error ->
                _connectionStatus.value = ConnectionStatus.Error(error)
            }
        )
    }

    /**
     * Connect to a specific Polar device
     */
    fun connectToPolarDevice(deviceId: String) {
        _connectionStatus.value = ConnectionStatus.Connecting

        if (polarSensorManager == null) {
            polarSensorManager = PolarSensorManager(application)
        }

        polarSensorManager?.connectToDevice(
            deviceId = deviceId,
            onConnected = {
                currentSensorManager = polarSensorManager
                _connectionStatus.value = ConnectionStatus.Connected
            },
            onError = { error ->
                _connectionStatus.value = ConnectionStatus.Error(error)
            }
        )
    }

    /**
     * Auto-connect to first available Polar device
     */
    fun connectToFirstPolarDevice() {
        _connectionStatus.value = ConnectionStatus.Connecting

        if (polarSensorManager == null) {
            polarSensorManager = PolarSensorManager(application)
        }

        polarSensorManager?.connect(
            onConnected = {
                currentSensorManager = polarSensorManager
                _connectionStatus.value = ConnectionStatus.Connected
            },
            onError = { error ->
                _connectionStatus.value = ConnectionStatus.Error(error)
            }
        )
    }

    fun getSensorManager(): SensorManager? = currentSensorManager

    override fun onCleared() {
        super.onCleared()
        polarSensorManager?.shutdown()
    }
}

sealed class ConnectionStatus {
    object Disconnected : ConnectionStatus()
    object Connecting : ConnectionStatus()
    object Connected : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

data class PolarDevice(
    val deviceId: String,
    val deviceName: String
)