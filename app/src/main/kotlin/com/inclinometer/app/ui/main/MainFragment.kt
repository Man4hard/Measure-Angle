package com.inclinometer.app.ui.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.inclinometer.app.R
import com.inclinometer.app.databinding.FragmentMainBinding
import com.inclinometer.app.ui.bubble.BubbleLevelFragment
import com.inclinometer.app.ui.camera.CameraFragment
import com.inclinometer.app.ui.calibration.CalibrationDialogFragment
import com.inclinometer.app.ui.digital.DigitalFragment
import com.inclinometer.app.viewmodel.InclinometerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InclinometerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupToolbar()
        viewModel.startSensor()
        observeUiState()
    }

    private fun setupViewPager() {
        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 3
            override fun createFragment(position: Int): Fragment = when (position) {
                0 -> BubbleLevelFragment()
                1 -> DigitalFragment()
                else -> CameraFragment()
            }
        }
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Bubble"
                1 -> "Digital"
                else -> "Camera"
            }
        }.attach()
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.main_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_theme -> { viewModel.toggleTheme(); requireActivity().recreate(); true }
                R.id.action_sound -> { viewModel.toggleSound(); true }
                R.id.action_calibrate -> {
                    CalibrationDialogFragment().show(childFragmentManager, CalibrationDialogFragment.TAG)
                    true
                }
                else -> false
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val soundItem = binding.toolbar.menu?.findItem(R.id.action_sound)
                    soundItem?.setIcon(if (state.isSoundEnabled) R.drawable.ic_sound_on else R.drawable.ic_sound_off)
                }
            }
        }
    }

    override fun onStop() { super.onStop(); viewModel.stopSensor() }
    override fun onStart() { super.onStart(); viewModel.startSensor() }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
