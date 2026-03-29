package com.inclinometer.app.data.local

import androidx.room.*
import com.inclinometer.app.data.model.Measurement
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<Measurement>>

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    suspend fun getAllMeasurementsList(): List<Measurement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: Measurement): Long

    @Delete
    suspend fun delete(measurement: Measurement)

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM measurements")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM measurements")
    suspend fun getCount(): Int
}
