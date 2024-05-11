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
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.blanco.somelai.R
import com.blanco.somelai.databinding.FragmentScanerCameraBinding
import java.nio.ByteBuffer
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit


// TODO camara y visualizacion mimiatura de la foto lista, ahora falta el escanner para extraer la imagen
class ScanerCameraFragment : Fragment() {

    private lateinit var _binding: FragmentScanerCameraBinding
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
        initializeFlashButtonIcon()
        setClicks()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setClicks(){
        // Set up the listeners for take photo and video capture buttons
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

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        Log.d(TAG, "Average luminosity: $luma")
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
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
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(requireActivity().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireActivity()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    // Carga la foto capturada en iv_photo_preview solo si output.savedUri no es nulo
                    output.savedUri?.let { uri ->
                        loadPhotoPreview(uri)
                    }
                }

            }
        )
    }

    private fun loadPhotoPreview(uri: Uri) {
        val imageView = binding.ivPhotoPreview
        val bitmap = uriToBitmap(uri)
        imageView.setImageBitmap(bitmap)
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }


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

private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun analyze(image: ImageProxy) {

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()

        listener(luma)

        image.close()
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
