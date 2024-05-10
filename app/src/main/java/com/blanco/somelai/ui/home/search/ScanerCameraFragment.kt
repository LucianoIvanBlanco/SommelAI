package com.blanco.somelai.ui.home.search

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.blanco.somelai.databinding.FragmentScanerCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit

class ScanerCameraFragment : Fragment() {

    private lateinit var _binding: FragmentScanerCameraBinding
    private val binding get() = _binding

    private val viewModel: WineViewModel by activityViewModels()

    // Camera X
    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanerCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listeners for take photo and video capture buttons
        binding.imageCaptureButton.setOnClickListener { takePhoto() }
        binding.videoCaptureButton.setOnClickListener { captureVideo() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {}

    private fun captureVideo() {}

    private fun startCamera() {}

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
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

//private val requestPermissionLauncher: ActivityResultLauncher<String> =
//    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//        if (isGranted) {
//            startCamera()
//        } else {
//            showDeniedPermissionMessage()
//            requireActivity().finish()
//        }
//    }
//
//private fun checkIfWeAlreadyHaveThisPermission() {
//    val cameraPermission: String = Manifest.permission.CAMERA
//    val permissionStatusCamera =
//        ContextCompat.checkSelfPermission(requireActivity(), cameraPermission)
//
//    if (permissionStatusCamera == PackageManager.PERMISSION_GRANTED) {
//        startCamera()
//    } else {
//        requestPermissionLauncher.launch(cameraPermission)
//    }
//}

//    private fun showPermissionRationaleDialog(cameraPermission: String) {
//        // Implementación del diálogo de razones de permiso
//    }
//
//
//    private fun showDeniedPermissionMessage() {
//        val message = getString(R.string.new_advertisement_denied_permission)
//        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
//    }


//private fun openCamera() {
//    val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//
//    cameraProviderFuture.addListener(Runnable {
//        val cameraProvider = cameraProviderFuture.get()
//        val preview = Preview.Builder().build().also {
//            it.setSurfaceProvider(binding.previewView.surfaceProvider)
//        }
//        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//        try {
//            cameraProvider.unbindAll()
//            cameraProvider.bindToLifecycle(this, cameraSelector, preview)
//        } catch(exc: Exception) {
//            Log.e("ERROR", "Use case binding failed", exc)
//        }
//    }, ContextCompat.getMainExecutor(requireContext()))
//}
//
//fun processImage(bitmap: Bitmap): TensorBuffer {
//    val imageProcessor = ImageProcessor.Builder().add(ResizeOp(224, 224, ResizeMethod.NEAREST_NEIGHBOUR)).build()
//    val tensorImage = TensorImage(DataType.UINT8)
//    tensorImage.load(bitmap)
//    tensorImage = imageProcessor.process(tensorImage)
//
//    val tensorBuffer = TensorBuffer.createFixedSize(tensorImage.shape, DataType.UINT8)
//    tensorBuffer.loadArray(tensorImage.buffer.array())
//
//    return tensorBuffer
