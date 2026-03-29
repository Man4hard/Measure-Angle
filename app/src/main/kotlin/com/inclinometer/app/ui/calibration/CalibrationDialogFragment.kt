package com.inclinometer.app.ui.calibration

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.inclinometer.app.databinding.DialogCalibrationBinding
import com.inclinometer.app.viewmodel.InclinometerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalibrationDialogFragment : DialogFragment() {

    private var _binding: DialogCalibrationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InclinometerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCalibrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val state = viewModel.uiState.value
        binding.tvCurrentPitch.text = "Pitch: %.2f°".format(state.sensorData.pitch)
        binding.tvCurrentRoll.text = "Roll: %.2f°".format(state.sensorData.roll)
        binding.tvCurrentYaw.text = "Yaw: %.2f°".format(state.sensorData.yaw)
        binding.tvOffsets.text = "Offsets: P=%.1f° R=%.1f° Y=%.1f°".format(
            state.calibrationOffset.pitchOffset,
            state.calibrationOffset.rollOffset,
            state.calibrationOffset.yawOffset
        )

        binding.btnCalibrateNow.setOnClickListener {
            viewModel.calibrate()
            dismiss()
        }
        binding.btnResetCalibration.setOnClickListener {
            viewModel.resetCalibration()
            dismiss()
        }
        binding.btnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CalibrationDialog"
    }
}
