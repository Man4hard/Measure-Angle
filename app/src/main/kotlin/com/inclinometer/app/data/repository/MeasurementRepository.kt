package com.inclinometer.app.data.repository

import com.inclinometer.app.data.local.MeasurementDao
import com.inclinometer.app.data.model.Measurement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementRepository @Inject constructor(
    private val dao: MeasurementDao
) {
    fun getAllMeasurements(): Flow<List<Measurement>> = dao.getAllMeasurements()

    suspend fun saveMeasurement(measurement: Measurement): Long = dao.insert(measurement)

    suspend fun deleteMeasurement(measurement: Measurement) = dao.delete(measurement)

    suspend fun deleteMeasurementById(id: Long) = dao.deleteById(id)

    suspend fun clearAllMeasurements() = dao.deleteAll()

    suspend fun getCount(): Int = dao.getCount()
}
