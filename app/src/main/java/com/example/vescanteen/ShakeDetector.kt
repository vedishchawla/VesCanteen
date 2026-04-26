package com.example.vescanteen

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Shake Detector — Uses accelerometer sensor to detect shake gestures.
 * Exp A1: Device sensor integration (accelerometer).
 *
 * How it works:
 * - Listens to accelerometer readings (x, y, z axis)
 * - Calculates acceleration magnitude
 * - If acceleration exceeds threshold, triggers shake callback
 */
class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    companion object {
        private const val SHAKE_THRESHOLD = 12.0f  // m/s² — force needed to trigger
        private const val SHAKE_COOLDOWN_MS = 1000  // Prevent rapid triggers
    }

    private var lastShakeTime: Long = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate total acceleration (minus gravity ~9.8)
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat() -
                SensorManager.GRAVITY_EARTH

        if (acceleration > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > SHAKE_COOLDOWN_MS) {
                lastShakeTime = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for shake detection
    }
}
