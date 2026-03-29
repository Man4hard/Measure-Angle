package com.inclinometer.app.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.inclinometer.app.data.model.SensorData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class SensorManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData

    private val _isSensorAvailable = MutableStateFlow(false)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable

    // Low-pass filter alpha (0.0 = very smooth, 1.0 = raw)
    private val ALPHA = 0.1f
    private val ALPHA_GYRO = 0.05f

    // Filtered accelerometer values
    private var filteredAccelX = 0f
    private var filteredAccelY = 0f
    private var filteredAccelZ = 9.81f

    // Complementary filter state
    private var compPitch = 0f
    private var compRoll = 0f
    private var lastTimestamp = 0L

    // Gyroscope values
    private var gyroPitch = 0f
    private var gyroRoll = 0f
    private var gyroYaw = 0f
    private var filteredYaw = 0f

    // Calibration offsets
    var calibrationOffset = com.inclinometer.app.data.model.CalibrationOffset()

    fun startListening() {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val hasAccel = accel != null
        val hasRotation = rotation != null

        _isSensorAvailable.value = hasAccel

        if (hasRotation) {
            sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME)
        } else if (hasAccel) {
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
            gyro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> handleRotationVector(event)
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event)
            Sensor.TYPE_GYROSCOPE -> handleGyroscope(event)
        }
    }

    private fun handleRotationVector(event: SensorEvent) {
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.getOrientation(rotationMatrix, orientation)

        val rawPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val rawRoll = Math.toDegrees(orientation[2].toDouble()).toFloat()
        val rawYaw = Math.toDegrees(orientation[0].toDouble()).toFloat()

        val pitch = lowPass(rawPitch, compPitch, ALPHA) - calibrationOffset.pitchOffset
        val roll = lowPass(rawRoll, compRoll, ALPHA) - calibrationOffset.rollOffset
        val yaw = lowPass(rawYaw, filteredYaw, ALPHA) - calibrationOffset.yawOffset

        compPitch = pitch + calibrationOffset.pitchOffset
        compRoll = roll + calibrationOffset.rollOffset
        filteredYaw = yaw + calibrationOffset.yawOffset

        _sensorData.value = _sensorData.value.copy(
            pitch = pitch,
            roll = roll,
            yaw = yaw
        )
    }

    private fun handleAccelerometer(event: SensorEvent) {
        filteredAccelX = lowPass(event.values[0], filteredAccelX, ALPHA)
        filteredAccelY = lowPass(event.values[1], filteredAccelY, ALPHA)
        filteredAccelZ = lowPass(event.values[2], filteredAccelZ, ALPHA)

        val accelPitch = Math.toDegrees(
            atan2(filteredAccelY.toDouble(), sqrt(filteredAccelX * filteredAccelX + filteredAccelZ * filteredAccelZ).toDouble())
        ).toFloat()
        val accelRoll = Math.toDegrees(
            atan2((-filteredAccelX).toDouble(), filteredAccelZ.toDouble())
        ).toFloat()

        val dt = if (lastTimestamp == 0L) 0f else (event.timestamp - lastTimestamp) / 1_000_000_000f
        lastTimestamp = event.timestamp

        if (dt > 0) {
            // Complementary filter: 98% gyro + 2% accelerometer
            compPitch = 0.98f * (compPitch + gyroPitch * dt) + 0.02f * accelPitch
            compRoll = 0.98f * (compRoll + gyroRoll * dt) + 0.02f * accelRoll
        } else {
            compPitch = accelPitch
            compRoll = accelRoll
        }

        val pitch = compPitch - calibrationOffset.pitchOffset
        val roll = compRoll - calibrationOffset.rollOffset
        val yaw = filteredYaw - calibrationOffset.yawOffset

        _sensorData.value = SensorData(
            pitch = pitch,
            roll = roll,
            yaw = yaw,
            accelX = filteredAccelX,
            accelY = filteredAccelY,
            accelZ = filteredAccelZ
        )
    }

    private fun handleGyroscope(event: SensorEvent) {
        gyroPitch = lowPass(event.values[1], gyroPitch, ALPHA_GYRO)
        gyroRoll = lowPass(event.values[2], gyroRoll, ALPHA_GYRO)
        gyroYaw = lowPass(event.values[0], gyroYaw, ALPHA_GYRO)

        val dt = if (lastTimestamp == 0L) 0.02f else (event.timestamp - lastTimestamp) / 1_000_000_000f
        filteredYaw += gyroYaw * dt
    }

    private fun lowPass(current: Float, previous: Float, alpha: Float): Float {
        return previous + alpha * (current - previous)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
