package com.inclinometer.app.ui.bubble

  import android.os.Bundle
  import android.view.LayoutInflater
  import android.view.View
  import android.view.ViewGroup
  import android.widget.Toast
  import androidx.fragment.app.Fragment
  import androidx.fragment.app.activityViewModels
  import androidx.lifecycle.Lifecycle
  import androidx.lifecycle.lifecycleScope
  import androidx.lifecycle.repeatOnLifecycle
  import com.inclinometer.app.databinding.FragmentBubbleLevelBinding
  import com.inclinometer.app.ui.calibration.CalibrationDialogFragment
  import com.inclinometer.app.viewmodel.InclinometerViewModel
  import dagger.hilt.android.AndroidEntryPoint
  import kotlinx.coroutines.launch

  @AndroidEntryPoint
  class BubbleLevelFragment : Fragment() {

      private var _binding: FragmentBubbleLevelBinding? = null
      private val binding get() = _binding!!
      private val viewModel: InclinometerViewModel by activityViewModels()

      override fun onCreateView(
          inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
      ): View {
          _binding = FragmentBubbleLevelBinding.inflate(inflater, container, false)
          return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
          super.onViewCreated(view, savedInstanceState)

          // Wire up in-view buttons
          binding.bubbleLevelView.onCalibrateClick = {
              CalibrationDialogFragment().show(childFragmentManager, CalibrationDialogFragment.TAG)
          }
          binding.bubbleLevelView.onSoundClick = {
              viewModel.toggleSound()
          }
          binding.bubbleLevelView.onLockClick = {
              binding.bubbleLevelView.isLocked = !binding.bubbleLevelView.isLocked
              val msg = if (binding.bubbleLevelView.isLocked) "Reading locked" else "Reading unlocked"
              Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
          }
          binding.bubbleLevelView.onSettingsClick = {
              viewModel.toggleTheme()
              requireActivity().recreate()
          }

          // Observe sensor data
          viewLifecycleOwner.lifecycleScope.launch {
              viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                  viewModel.sensorData.collect { data ->
                      if (!binding.bubbleLevelView.isLocked) {
                          binding.bubbleLevelView.updateAngles(data.pitch, data.roll)
                      }
                  }
              }
          }

          // Sync sound state icon
          viewLifecycleOwner.lifecycleScope.launch {
              viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                  viewModel.uiState.collect { state ->
                      binding.bubbleLevelView.isSoundEnabled = state.isSoundEnabled
                      binding.bubbleLevelView.invalidate()
                  }
              }
          }
      }

      override fun onDestroyView() {
          super.onDestroyView()
          _binding = null
      }
  }
  