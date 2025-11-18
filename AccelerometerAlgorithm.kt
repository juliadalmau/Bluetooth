package com.example.bluetooth.model.algorithms

import com.example.bluetooth.model.data.SensorData
import com.example.bluetooth.utils.VectorMath


/**
 * Algorithm 1: Linear Acceleration with EWMA Filter
 *
 * Steps:
 * 1. Calculate angle from accelerometer using physics
 * 2. Apply EWMA filter to smooth the computed angle
 *
 * EWMA Formula: y(n) = α * x(n) + (1 - α) * y(n-1)
 * where:
 *   - x(n) is the current computed angle
 *   - y(n-1) is the previous filtered output
 *   - α is the filter factor (0 to 1)
 */
class AccelerometerAlgorithm(private val alpha: Float = 0.2f) : ElevationCalculator {

    private var previousFilteredAngle: Float = 0f
    private var isFirstReading = true

    init {
        require(alpha in 0f..1f) { "Alpha must be between 0 and 1" }
    }

    override fun calculateElevation(sensorData: SensorData): Float {
        // Step 1: Calculate raw angle from accelerometer using physics/vectors
        val rawAngle = VectorMath.calculateElevationFromAccelerometer(
            sensorData.accelerometerX,
            sensorData.accelerometerY,
            sensorData.accelerometerZ
        )

        // Step 2: Apply EWMA filter to the computed angle to remove noise
        // y(n) = α * x(n) + (1 - α) * y(n-1)
        val filteredAngle = if (isFirstReading) {
            // First reading: no previous value, just use raw angle
            isFirstReading = false
            previousFilteredAngle = rawAngle
            rawAngle
        } else {
            // Apply EWMA formula
            alpha * rawAngle + (1 - alpha) * previousFilteredAngle
        }

        // Store for next iteration
        previousFilteredAngle = filteredAngle

        return filteredAngle
    }

    override fun reset() {
        previousFilteredAngle = 0f
        isFirstReading = true
    }
}