package com.inclinometer.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pitch: Float,
    val roll: Float,
    val yaw: Float,
    val label: String,
    val mode: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SensorData(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val yaw: Float = 0f,
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f
)

data class CalibrationOffset(
    val pitchOffset: Float = 0f,
    val rollOffset: Float = 0f,
    val yawOffset: Float = 0f
)
