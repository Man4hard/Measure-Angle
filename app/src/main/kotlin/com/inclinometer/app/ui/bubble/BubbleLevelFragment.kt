package com.inclinometer.app.ui.bubble

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.inclinometer.app.databinding.FragmentBubbleLevelBinding
import com.inclinometer.app.viewmodel.InclinometerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class BubbleLevelFragment : Fragment() {

    private var _binding: FragmentBubbleLevelBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InclinometerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBubbleLevelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUiState()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val pitch = state.sensorData.pitch
                    val roll = state.sensorData.roll
                    binding.bubbleLevelView.updateAngles(pitch, roll)
                    binding.tvPitch.text = "%.1f°".format(pitch)
                    binding.tvRoll.text = "%.1f°".format(roll)

                    val tilt = Math.sqrt((pitch * pitch + roll * roll).toDouble()).toFloat()
                    val statusText: String
                    val statusColor: Int
                    when {
                        tilt < 0.5f -> {
                            statusText = "LEVEL ✓"
                            statusColor = android.graphics.Color.parseColor("#2ECC71")
                        }
                        tilt < 2f -> {
                            statusText = "NEARLY LEVEL"
                            statusColor = android.graphics.Color.parseColor("#F39C12")
                        }
                        tilt < 5f -> {
                            statusText = "SLIGHT TILT"
                            statusColor = android.graphics.Color.parseColor("#E67E22")
                        }
                        else -> {
                            statusText = "NOT LEVEL"
                            statusColor = android.graphics.Color.parseColor("#E74C3C")
                        }
                    }
                    binding.tvStatus.text = statusText
                    binding.tvStatus.setTextColor(statusColor)

                    if (!state.isSensorAvailable) {
                        binding.tvDemoMode.visibility = View.VISIBLE
                    } else {
                        binding.tvDemoMode.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
