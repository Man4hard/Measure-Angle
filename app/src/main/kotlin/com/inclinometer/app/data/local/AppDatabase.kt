package com.inclinometer.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.inclinometer.app.data.model.Measurement

@Database(
    entities = [Measurement::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao

    companion object {
        const val DATABASE_NAME = "inclinometer_db"
    }
}
