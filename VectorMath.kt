package com.example.bluetooth.utils

import kotlin.math.acos
import kotlin.math.sqrt

object VectorMath {
    /**
     * Calculate elevation angle from accelerometer data
     * Assumes phone is strapped to arm with screen facing outward
     *
     * @return angle in degrees (0-90)
     */
    fun calculateElevationFromAccelerometer(
        ax: Float,
        ay: Float,
        az: Float
    ): Float {
        // Calculate magnitude of acceleration vector
        val magnitude = sqrt(ax * ax + ay * ay + az * az)

        // Normalize the vector
        val axNorm = ax / magnitude
        val ayNorm = ay / magnitude
        val azNorm = az / magnitude

        // Calculate angle relative to gravity
        // When arm is at rest (0°), gravity points down (-Y axis for typical phone orientation)
        // When arm is at 90°, gravity points along the arm axis

        // This depends on phone orientation - adjust based on your setup
        // Example: if Y-axis points along the arm when at rest
        val angle = Math.toDegrees(acos(ayNorm.toDouble())).toFloat()

        // Clamp between 0 and 90 degrees
        return angle.coerceIn(0f, 90f)
    }

    /**
     * Extract relevant gyroscope component for arm elevation
     * Usually rotation around X-axis for arm abduction/adduction
     */
    fun getRelevantGyroscopeComponent(
        gx: Float,
        gy: Float,
        gz: Float
    ): Float {
        // Return the component that corresponds to arm elevation
        // Typically rotation around X-axis for abduction
        return gx
    }
}