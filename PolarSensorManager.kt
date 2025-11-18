package com.example.bluetooth.model.sensors

import android.content.Context
import android.util.Log
import com.example.bluetooth.model.data.SensorData
import com.example.bluetooth.model.data.SensorType
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.UUID

/**
 * PolarSensorManager compatible con Polar SDK 5.1.0
 */
class PolarSensorManager(private val context: Context) : SensorManager {

    companion object {
        private const val TAG = "PolarSensorManager"
    }

    // Inicialización del API de Polar SDK 5.1.0
    private val api: PolarBleApi by lazy {
        PolarBleApiDefaultImpl.defaultImplementation(
            context,
            setOf(
                PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
                PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
            )
        )
    }
    private var connectedDeviceId: String? = null
    private var dataCallback: ((SensorData) -> Unit)? = null

    // Últimas lecturas de sensores
    private val latestAcc = FloatArray(3) { 0f }
    private val latestGyro = FloatArray(3) { 0f }

    // Disposables para gestionar suscripciones RxJava
    private var scanDisposable: Disposable? = null
    private var accDisposable: Disposable? = null
    private var gyroDisposable: Disposable? = null

    init {
        // Configurar callbacks para SDK 5.1.0
        api.setApiCallback(object : PolarBleApiCallback() {
            override fun blePowerStateChanged(powered: Boolean) {
                Log.d(TAG, "BLE power: $powered")
            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTED: ${polarDeviceInfo.deviceId}")
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTING: ${polarDeviceInfo.deviceId}")
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "DISCONNECTED: ${polarDeviceInfo.deviceId}")
                if (connectedDeviceId == polarDeviceInfo.deviceId) {
                    connectedDeviceId = null
                }
            }

            override fun bleSdkFeatureReady(
                identifier: String,
                feature: PolarBleApi.PolarBleSdkFeature
            ) {
                Log.d(TAG, "Feature $feature ready for: $identifier")
                // En SDK 5.x, cuando FEATURE_POLAR_ONLINE_STREAMING está listo,
                // podemos iniciar el streaming
            }

            override fun disInformationReceived(
                identifier: String,
                uuid: UUID,
                value: String
            ) {
                Log.d(TAG, "DIS INFO uuid: $uuid value: $value")
            }

            override fun batteryLevelReceived(identifier: String, level: Int) {
                Log.d(TAG, "BATTERY LEVEL: $level")
            }
        })
    }

    // ----------------------------
    // CONNECTION
    // ----------------------------
    override fun isAvailable(): Boolean = true

    override fun connect(onConnected: () -> Unit, onError: (String) -> Unit) {
        if (connectedDeviceId != null) {
            onConnected()
            return
        }

        // Buscar y conectar al primer dispositivo Polar encontrado
        scanDisposable = api.searchForDevice()
            .observeOn(AndroidSchedulers.mainThread())
            .firstOrError()
            .subscribe(
                { polarDeviceInfo ->
                    Log.d(TAG, "Found device: ${polarDeviceInfo.name}")
                    try {
                        api.connectToDevice(polarDeviceInfo.deviceId)
                        connectedDeviceId = polarDeviceInfo.deviceId
                        onConnected()
                    } catch (e: Exception) {
                        Log.e(TAG, "Connection failed", e)
                        onError("Connection failed: ${e.message}")
                    }
                },
                { error ->
                    Log.e(TAG, "Scan error", error)
                    onError("Scan error: ${error.message}")
                }
            )
    }

    fun connectToDevice(deviceId: String, onConnected: () -> Unit, onError: (String) -> Unit) {
        try {
            api.connectToDevice(deviceId)
            connectedDeviceId = deviceId
            onConnected()
        } catch (e: Exception) {
            Log.e(TAG, "Connect error", e)
            onError(e.message ?: "Connect failed")
        }
    }

    override fun disconnect() {
        connectedDeviceId?.let { id ->
            try {
                api.disconnectFromDevice(id)
            } catch (e: Exception) {
                Log.e(TAG, "Disconnect error", e)
            }
        }
        stopListening()
        connectedDeviceId = null
    }

    // ----------------------------
    // SENSOR STREAM
    // ----------------------------
    override fun startListening(callback: (SensorData) -> Unit) {
        val deviceId = connectedDeviceId ?: run {
            Log.e(TAG, "No device connected")
            return
        }

        dataCallback = callback

        // Iniciar streaming de acelerómetro y giroscopio
        startAccelerometer(deviceId)
        startGyroscope(deviceId)
    }

    private fun startAccelerometer(deviceId: String) {
        // En SDK 5.x, el streaming se hace directamente
        accDisposable = api.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.ACC)
            .toFlowable()
            .flatMap { settings ->
                // Usar configuración máxima disponible
                val setting = settings.maxSettings()
                Log.d(TAG, "ACC settings: $setting")
                api.startAccStreaming(deviceId, setting)
            }
            .observeOn(Schedulers.io())
            .subscribe(
                { polarAccData ->
                    // Procesar cada muestra
                    for (sample in polarAccData.samples) {
                        // Convertir de miligramos a m/s²
                        latestAcc[0] = sample.x.toFloat() / 1000f * 9.81f
                        latestAcc[1] = sample.y.toFloat() / 1000f * 9.81f
                        latestAcc[2] = sample.z.toFloat() / 1000f * 9.81f
                        emitSensorData(sample.timeStamp)
                    }
                },
                { error ->
                    Log.e(TAG, "ACC streaming error", error)
                }
            )
    }

    private fun startGyroscope(deviceId: String) {
        gyroDisposable = api.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.GYRO)
            .toFlowable()
            .flatMap { settings ->
                val setting = settings.maxSettings()
                Log.d(TAG, "GYRO settings: $setting")
                api.startGyroStreaming(deviceId, setting)
            }
            .observeOn(Schedulers.io())
            .subscribe(
                { polarGyroData ->
                    // Procesar cada muestra (degrees/s)
                    for (sample in polarGyroData.samples) {
                        latestGyro[0] = sample.x.toFloat()
                        latestGyro[1] = sample.y.toFloat()
                        latestGyro[2] = sample.z.toFloat()
                        emitSensorData(sample.timeStamp)
                    }
                },
                { error ->
                    Log.e(TAG, "GYRO streaming error", error)
                }
            )
    }

    override fun stopListening() {
        accDisposable?.dispose()
        gyroDisposable?.dispose()
        accDisposable = null
        gyroDisposable = null
        dataCallback = null
    }

    override fun getSensorType(): SensorType = SensorType.POLAR_VERITY_SENSE

    // ----------------------------
    // HELPERS
    // ----------------------------
    private fun emitSensorData(timestamp: Long) {
        dataCallback?.invoke(
            SensorData(
                timestamp = timestamp,
                sensorType = SensorType.POLAR_VERITY_SENSE,
                accelerometerX = latestAcc[0],
                accelerometerY = latestAcc[1],
                accelerometerZ = latestAcc[2],
                gyroscopeX = latestGyro[0],
                gyroscopeY = latestGyro[1],
                gyroscopeZ = latestGyro[2]
            )
        )
    }

    fun shutdown() {
        stopListening()
        scanDisposable?.dispose()
        connectedDeviceId?.let { api.disconnectFromDevice(it) }
        api.shutDown()
    }

    /**
     * Escanea dispositivos Polar disponibles
     */
    fun scanForDevices(
        onDeviceFound: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        scanDisposable?.dispose()

        scanDisposable = api.searchForDevice()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { polarDeviceInfo ->
                    if (polarDeviceInfo.name.startsWith("Polar", ignoreCase = true)) {
                        onDeviceFound(polarDeviceInfo.deviceId, polarDeviceInfo.name)
                    }
                },
                { error ->
                    Log.e(TAG, "Scan error", error)
                    onError(error.message ?: "Scan failed")
                },
                {
                    Log.d(TAG, "Scan completed")
                }
            )
    }

    fun stopScanning() {
        scanDisposable?.dispose()
        scanDisposable = null
    }
}