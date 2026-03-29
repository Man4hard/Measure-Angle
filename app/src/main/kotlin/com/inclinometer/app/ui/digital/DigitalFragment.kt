package com.inclinometer.app.ui.digital

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.inclinometer.app.R
import com.inclinometer.app.databinding.FragmentDigitalBinding
import com.inclinometer.app.viewmodel.InclinometerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class DigitalFragment : Fragment() {

    private var _binding: FragmentDigitalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InclinometerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDigitalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeUiState()
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            showSaveDialog()
        }
    }

    private fun showSaveDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter label (optional)"
        AlertDialog.Builder(requireContext())
            .setTitle("Save Measurement")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val label = input.text.toString().ifBlank { "Measurement" }
                viewModel.saveMeasurement(label, "Digital")
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root, "Measurement saved!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val data = state.sensorData
                    binding.tvPitchValue.text = "%.2f°".format(data.pitch)
                    binding.tvRollValue.text = "%.2f°".format(data.roll)
                    binding.tvYawValue.text = "%.2f°".format(data.yaw)

                    binding.tvAccelX.text = "X: %.3f m/s²".format(data.accelX)
                    binding.tvAccelY.text = "Y: %.3f m/s²".format(data.accelY)
                    binding.tvAccelZ.text = "Z: %.3f m/s²".format(data.accelZ)

                    // Update progress bars (-90 to +90 → 0 to 100)
                    binding.progressPitch.progress = ((data.pitch + 90f) / 180f * 100).toInt().coerceIn(0, 100)
                    binding.progressRoll.progress = ((data.roll + 90f) / 180f * 100).toInt().coerceIn(0, 100)
                    binding.progressYaw.progress = ((data.yaw + 180f) / 360f * 100).toInt().coerceIn(0, 100)

                    // Color the pitch/roll based on tilt level
                    val pitchAbs = abs(data.pitch)
                    val rollAbs = abs(data.roll)
                    binding.tvPitchValue.setTextColor(angleColor(pitchAbs))
                    binding.tvRollValue.setTextColor(angleColor(rollAbs))

                    if (!state.isSensorAvailable) {
                        binding.tvSensorStatus.text = "Demo Mode — No sensor"
                        binding.tvSensorStatus.setTextColor(android.graphics.Color.parseColor("#F39C12"))
                    } else {
                        binding.tvSensorStatus.text = "● Sensor Active"
                        binding.tvSensorStatus.setTextColor(android.graphics.Color.parseColor("#2ECC71"))
                    }
                }
            }
        }
    }

    private fun angleColor(angle: Float): Int = when {
        angle < 1f -> android.graphics.Color.parseColor("#2ECC71")
        angle < 5f -> android.graphics.Color.parseColor("#F39C12")
        else -> android.graphics.Color.parseColor("#E74C3C")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
