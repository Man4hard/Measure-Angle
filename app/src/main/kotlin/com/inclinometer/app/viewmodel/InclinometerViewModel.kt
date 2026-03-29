package com.inclinometer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inclinometer.app.data.model.CalibrationOffset
import com.inclinometer.app.data.model.Measurement
import com.inclinometer.app.data.model.SensorData
import com.inclinometer.app.data.repository.MeasurementRepository
import com.inclinometer.app.util.CalibrationManager
import com.inclinometer.app.util.SensorManagerHelper
import com.inclinometer.app.util.SoundManager
import com.inclinometer.app.util.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InclinometerMode { BUBBLE, DIGITAL, CAMERA }

data class InclinometerUiState(
    val sensorData: SensorData = SensorData(),
    val isSensorAvailable: Boolean = false,
    val calibrationOffset: CalibrationOffset = CalibrationOffset(),
    val mode: InclinometerMode = InclinometerMode.BUBBLE,
    val isSoundEnabled: Boolean = true,
    val isDarkMode: Boolean = true,
    val isLevel: Boolean = false,
    val wasLevel: Boolean = false
)

@HiltViewModel
class InclinometerViewModel @Inject constructor(
    private val repository: MeasurementRepository,
    private val sensorHelper: SensorManagerHelper,
    private val soundManager: SoundManager,
    private val calibrationManager: CalibrationManager,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(InclinometerUiState())
    val uiState: StateFlow<InclinometerUiState> = _uiState.asStateFlow()

    val measurements = repository.getAllMeasurements()

    init {
        loadCalibration()
        observeSensorData()
        _uiState.update {
            it.copy(
                isSoundEnabled = soundManager.isSoundEnabled,
                isDarkMode = themeManager.isDarkMode
            )
        }
    }

    private fun loadCalibration() {
        val offset = calibrationManager.loadCalibration()
        sensorHelper.calibrationOffset = offset
        _uiState.update { it.copy(calibrationOffset = offset) }
    }

    private fun observeSensorData() {
        viewModelScope.launch {
            sensorHelper.sensorData.collect { data ->
                val isLevel = kotlin.math.abs(data.pitch) < 0.5f && kotlin.math.abs(data.roll) < 0.5f
                val wasLevel = _uiState.value.isLevel
                if (isLevel && !wasLevel) {
                    soundManager.playLevelBeep()
                }
                _uiState.update { state ->
                    state.copy(
                        sensorData = data,
                        isLevel = isLevel,
                        wasLevel = wasLevel
                    )
                }
            }
        }
        viewModelScope.launch {
            sensorHelper.isSensorAvailable.collect { available ->
                _uiState.update { it.copy(isSensorAvailable = available) }
            }
        }
    }

    fun startSensor() = sensorHelper.startListening()

    fun stopSensor() = sensorHelper.stopListening()

    fun setMode(mode: InclinometerMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun calibrate() {
        val current = _uiState.value.sensorData
        val newOffset = CalibrationOffset(
            pitchOffset = current.pitch + _uiState.value.calibrationOffset.pitchOffset,
            rollOffset = current.roll + _uiState.value.calibrationOffset.rollOffset,
            yawOffset = current.yaw + _uiState.value.calibrationOffset.yawOffset
        )
        calibrationManager.saveCalibration(newOffset)
        sensorHelper.calibrationOffset = newOffset
        _uiState.update { it.copy(calibrationOffset = newOffset) }
        soundManager.playCalibrationSound()
        soundManager.vibrate(100)
    }

    fun resetCalibration() {
        val zero = CalibrationOffset()
        calibrationManager.saveCalibration(zero)
        sensorHelper.calibrationOffset = zero
        _uiState.update { it.copy(calibrationOffset = zero) }
        soundManager.playCalibrationSound()
    }

    fun saveMeasurement(label: String, mode: String) {
        val data = _uiState.value.sensorData
        viewModelScope.launch {
            repository.saveMeasurement(
                Measurement(
                    pitch = data.pitch,
                    roll = data.roll,
                    yaw = data.yaw,
                    label = label,
                    mode = mode
                )
            )
            soundManager.playSaveClick()
            soundManager.vibrate(50)
        }
    }

    fun deleteMeasurement(id: Long) {
        viewModelScope.launch { repository.deleteMeasurementById(id) }
    }

    fun clearAllMeasurements() {
        viewModelScope.launch { repository.clearAllMeasurements() }
    }

    fun toggleSound() {
        soundManager.isSoundEnabled = !soundManager.isSoundEnabled
        _uiState.update { it.copy(isSoundEnabled = soundManager.isSoundEnabled) }
    }

    fun toggleTheme() {
        themeManager.toggleTheme()
        _uiState.update { it.copy(isDarkMode = themeManager.isDarkMode) }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
