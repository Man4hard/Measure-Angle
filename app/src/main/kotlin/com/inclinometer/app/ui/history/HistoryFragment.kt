package com.inclinometer.app.ui.history

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.inclinometer.app.databinding.FragmentHistoryBinding
import com.inclinometer.app.viewmodel.InclinometerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InclinometerViewModel by activityViewModels()
    private lateinit var adapter: MeasurementAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeMeasurements()
    }

    private fun setupRecyclerView() {
        adapter = MeasurementAdapter { measurement ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Measurement")
                .setMessage("Delete \"${measurement.label}\"?")
                .setPositiveButton("Delete") { _, _ -> viewModel.deleteMeasurement(measurement.id) }
                .setNegativeButton("Cancel", null)
                .show()
        }
        binding.rvMeasurements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMeasurements.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnClearAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear All")
                .setMessage("Delete all saved measurements?")
                .setPositiveButton("Clear All") { _, _ -> viewModel.clearAllMeasurements() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun observeMeasurements() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.measurements.collect { measurements ->
                    adapter.submitList(measurements)
                    if (measurements.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.rvMeasurements.visibility = View.GONE
                        binding.btnClearAll.isEnabled = false
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.rvMeasurements.visibility = View.VISIBLE
                        binding.btnClearAll.isEnabled = true
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
