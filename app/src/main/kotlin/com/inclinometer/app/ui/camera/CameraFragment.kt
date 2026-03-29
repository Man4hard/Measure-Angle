package com.inclinometer.app.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.inclinometer.app.databinding.FragmentCameraBinding
import com.inclinometer.app.viewmodel.InclinometerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InclinometerViewModel by activityViewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera() else showPermissionDenied()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnGrantPermission.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        checkCameraPermission()
        observeUiState()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> startCamera()
            else -> showPermissionRequest()
        }
    }

    private fun startCamera() {
        binding.permissionLayout.visibility = View.GONE
        binding.cameraPreview.visibility = View.VISIBLE
        binding.overlayView.visibility = View.VISIBLE

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun showPermissionRequest() {
        binding.permissionLayout.visibility = View.VISIBLE
        binding.cameraPreview.visibility = View.GONE
        binding.overlayView.visibility = View.GONE
        binding.tvPermissionMessage.text = "Camera permission is required for overlay mode."
        binding.btnGrantPermission.text = "Grant Permission"
    }

    private fun showPermissionDenied() {
        binding.tvPermissionMessage.text = "Camera permission denied. Please enable it in Settings."
        binding.btnGrantPermission.visibility = View.GONE
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val pitch = state.sensorData.pitch
                    val roll = state.sensorData.roll
                    binding.overlayView.updateAngles(pitch, roll)
                    binding.tvOverlayPitch.text = "Pitch: %.1f°".format(pitch)
                    binding.tvOverlayRoll.text = "Roll: %.1f°".format(roll)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
