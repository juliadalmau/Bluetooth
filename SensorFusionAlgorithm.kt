package com.example.bluetooth.model.algorithms

import com.example.bluetooth.model.data.SensorData
import com.example.bluetooth.utils.VectorMath


/**
 * Algorithm 2: Sensor Fusion with Complementary Filter
 *
 * Steps:
 * 1. Calculate angle from accelerometer (stable but noisy)
 * 2. Calculate angle from gyroscope integration (smooth but drifts)
 * 3. Apply complementary filter to combine both measurements
 *
 * Complementary Filter Formula: y(n) = α * x_g(n) + (1 - α) * x_a(n)
 * where:
 *   - x_a(n) is the angle from accelerometer
 *   - x_g(n) is the angle from gyroscope integration
 *   - α is the filter factor (typically 0.95-0.98 to trust gyro more short-term)
 */
class SensorFusionAlgorithm(private val alpha: Float = 0.98f) : ElevationCalculator {

    private var previousAngle: Float = 0f
    private var previousTimestamp: Long = 0L

    init {
        require(alpha in 0f..1f) { "Alpha must be between 0 and 1" }
    }

    override fun calculateElevation(sensorData: SensorData): Float {
        // Step 1: Calculate angle from accelerometer - x_a(n)
        val accelerometerAngle = VectorMath.calculateElevationFromAccelerometer(
            sensorData.accelerometerX,
            sensorData.accelerometerY,
            sensorData.accelerometerZ
        )

        // Check if gyroscope data is available
        val gyroscopeAngularVelocity = sensorData.gyroscopeX
            ?: return accelerometerAngle // Fallback to accelerometer only if no gyro

        // Step 2: Calculate angle from gyroscope integration - x_g(n)
        val gyroscopeAngle = if (previousTimestamp == 0L) {
            // First reading: initialize with accelerometer angle
            previousTimestamp = sensorData.timestamp
            previousAngle = accelerometerAngle
            accelerometerAngle
        } else {
            // Calculate time delta in seconds
            val dt = (sensorData.timestamp - previousTimestamp) / 1_000_000_000.0f

            // Integrate gyroscope: angle = previous_angle + angular_velocity * dt
            // This is x_g(n) - the "angular input from the gyroscope"
            previousAngle + (gyroscopeAngularVelocity * dt)
        }

        // Step 3: Apply complementary filter to fuse both sensors
        // y(n) = α * x_g(n) + (1 - α) * x_a(n)
        val fusedAngle = alpha * gyroscopeAngle + (1 - alpha) * accelerometerAngle

        // Update state for next iteration
        previousAngle = fusedAngle
        previousTimestamp = sensorData.timestamp

        return fusedAngle
    }

    override fun reset() {
        previousAngle = 0f
        previousTimestamp = 0L
    }
}