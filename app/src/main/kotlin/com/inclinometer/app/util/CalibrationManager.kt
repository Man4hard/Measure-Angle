package com.inclinometer.app.util

import android.content.Context
import com.inclinometer.app.data.model.CalibrationOffset
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalibrationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val prefs = context.getSharedPreferences("calibration_prefs", Context.MODE_PRIVATE)

    fun saveCalibration(offset: CalibrationOffset) {
        prefs.edit().putString("calibration", gson.toJson(offset)).apply()
    }

    fun loadCalibration(): CalibrationOffset {
        val json = prefs.getString("calibration", null) ?: return CalibrationOffset()
        return try {
            gson.fromJson(json, CalibrationOffset::class.java)
        } catch (e: Exception) {
            CalibrationOffset()
        }
    }

    fun resetCalibration() {
        prefs.edit().remove("calibration").apply()
    }
}
