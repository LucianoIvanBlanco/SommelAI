package com.blanco.somelai.ui.home.search

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.blanco.somelai.R
import com.blanco.somelai.databinding.FragmentScannerCameraBinding
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerCameraFragment : Fragment() {

    private lateinit var _binding: FragmentScannerCameraBinding
    private val binding get() = _binding

    private val viewModel: WineViewModel by activityViewModels()

    // Camera X
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScannerCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
        initializeFlashButtonIcon()
        setClicks()
        cameraExecutor = Executors.newSingleThreadExecutor()

        observeViewModel()
    }

    private fun requestPermissions() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    // Observamos cambios en los livedata para navegar
    private fun observeViewModel() {
        viewModel.navigateToWineList.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                findNavController().navigate(R.id.action_scannerCameraFragment_to_wineListFragment)
                viewModel.resetNavigateToWineList() // Resetear el evento
            }
        })
        viewModel.navigateToWineFeed.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                findNavController().navigate(R.id.action_scannerCameraFragment_to_feedFragment)
                viewModel.resetNavigateToFeedFragment() // Resetear el evento
            }
        })

    }

    private fun setClicks(){
        binding.imageCaptureButton.setOnClickListener {
            takePhoto()
        }
        binding.closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.flashButton.setOnClickListener {
            toggleFlash()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("ScannerCameraFragment", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun toggleFlash() {
        val camera = this.camera ?: return

        val torchState = camera.cameraInfo.torchState.value
        val isTorchOn = torchState == TorchState.ON

        camera.cameraControl.enableTorch(!isTorchOn).addListener({
            updateFlashButtonIcon(!isTorchOn)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun updateFlashButtonIcon(isTorchOn: Boolean) {
        val iconResId = if (isTorchOn) R.drawable.ic_flash_on else R.drawable.ic_flash_off
        binding.flashButton.setIconResource(iconResId)
    }

    private fun initializeFlashButtonIcon() {
        val camera = this.camera ?: return

        camera.cameraInfo.torchState.observe(viewLifecycleOwner) { state ->
            val isTorchOn = state == TorchState.ON
            updateFlashButtonIcon(isTorchOn)
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SomelAI")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireActivity().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireActivity()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("ScannerCameraFragment", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Log.d("ScannerCameraFragment", msg)
                    output.savedUri?.let { uri ->
                        loadPhotoPreview(uri)
                    }
                }
            }
        )
    }

    private fun loadPhotoPreview(uri: Uri) {
        val bitmap = uriToBitmap(uri)
        binding.ibPhotoPreview.setImageBitmap(bitmap)
        binding.ibPhotoPreview.setOnClickListener {
            viewModel.getWinesAndFilterByName(uri, requireContext())
            Toast.makeText(context, "INICIANDO BUSQUEDA...", Toast.LENGTH_LONG).show()
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "SomelAI"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    // Permisos

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireActivity(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
        }
    }
}
