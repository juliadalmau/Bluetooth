package com.example.bluetooth.utils

import com.example.bluetooth.model.sensors.SensorManager

object Constants {
    // Filter parameters
    const val EWMA_ALPHA_DEFAULT = 0.2f
    const val COMPLEMENTARY_ALPHA_DEFAULT = 0.98f

    // Sensor sampling rate
    const val SENSOR_DELAY = android.hardware.SensorManager.SENSOR_DELAY_GAME

    // Graph settings
    const val MAX_GRAPH_POINTS = 1000
    const val GRAPH_Y_MIN = 0f
    const val GRAPH_Y_MAX = 100f

    // CSV export
    const val CSV_HEADER = "Timestamp,Algorithm1_Angle,Algorithm2_Angle"
}