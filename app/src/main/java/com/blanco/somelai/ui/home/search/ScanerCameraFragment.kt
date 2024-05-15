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
import com.blanco.somelai.R
import com.blanco.somelai.databinding.FragmentScanerCameraBinding
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


// TODO camara y visualizacion mimiatura de la foto lista, ahora falta el escanner para extraer la imagen
class ScanerCameraFragment : Fragment() {

    private lateinit var _binding: FragmentScanerCameraBinding
    private val binding get() = _binding

    private val viewModel: WineViewModel by activityViewModels()

    // Camera X
    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private var camera: Camera? = null

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

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

    // CAMARA

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

            // Configuración de la cámara sin ImageAnalysis
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
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
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
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
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    output.savedUri?.let { uri ->
                        loadPhotoPreview(uri)
                    }
                }
            }
        )
    }

    private fun loadPhotoPreview(uri: Uri) {
        val bitmap = uriToBitmap(uri) // Asegúrate de que esta función convierte correctamente la URI a Bitmap
        binding.ibPhotoPreview.setImageBitmap(bitmap)
        binding.ibPhotoPreview.setOnClickListener {
            viewModel.getWinesAndFilterByName(uri, requireContext())
        }
    }




//    private fun recognizeTextFromImage(uri: Uri) {
//        val image = InputImage.fromFilePath(requireContext(), uri)
//        recognizer.process(image)
//            .addOnSuccessListener { visionText ->
//                // Utilizar directamente el texto completo reconocido
//                val fullText = visionText.text
//
//                // Dividir el texto en palabras individuales
//                val words = fullText.split("\\s+".toRegex()).filter { it.isNotEmpty() }
//
//                // Convertir la lista de palabras a un solo string separado por comas para la búsqueda
//                val queryText = words.joinToString(" ")
//
//                Log.d(TAG, "Recognized text: $queryText")
//                searchWine(queryText)
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error recognizing text: ${e.localizedMessage}", e)
//            }
//    }

//    private fun recognizeTextFromImage(uri: Uri) {
//        val image = InputImage.fromFilePath(requireContext(), uri)
//        recognizer.process(image)
//            .addOnSuccessListener { visionText ->
//                Log.d(TAG, "Text blocks detected: ${visionText.textBlocks.size}")
//                if (visionText.textBlocks.isEmpty()) {
//                    Log.d(TAG, "No text blocks detected.")
//                }
//
//                val largestTexts = getLargestTexts(visionText, image)
//                val resultText = largestTexts.joinToString(" ") { it.text }
//                Log.d(TAG, "Recognized text: $resultText")
//                searchWine(resultText)
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error recognizing text: ${e.localizedMessage}", e)
//            }
//    }
//
//    private fun getLargestTexts(visionText: Text, image: InputImage): List<Text.TextBlock> {
//        val largestTextBlocks = mutableListOf<Text.TextBlock>()
//        var maxSize = 0.0
//
//        val imageWidth = image.width.toDouble()
//        val imageHeight = image.height.toDouble()
//        val imageArea = imageWidth * imageHeight
//
//        val minProportion = 0.01  // Ajustado a 1% para capturar bloques más pequeños
//
//        for (block in visionText.textBlocks) {
//            val boundingBox = block.boundingBox
//            if (boundingBox != null) {
//                val width = boundingBox.width()?.toDouble() ?: 0.0
//                val height = boundingBox.height()?.toDouble() ?: 0.0
//                val blockSize = width * height
//                val blockProportion = blockSize / imageArea
//
//                Log.d(TAG, "Block size: $blockSize, Proportion: $blockProportion")
//
//                if (blockProportion > minProportion) {
//                    if (blockSize > maxSize) {
//                        largestTextBlocks.clear()
//                        maxSize = blockSize
//                        largestTextBlocks.add(block)
//                    } else if (blockSize == maxSize) {
//                        largestTextBlocks.add(block)
//                    }
//                }
//            }
//        }
//
//        return largestTextBlocks
//    }
//



//    private fun getLargestTexts(visionText: Text): List<Text.TextBlock> {
//        val largestTextBlocks = mutableListOf<Text.TextBlock>()
//        var maxSize = 0.0  // Usar Double directamente
//
//        // Asumiendo una densidad de píxeles estándar de 160 DPI (2.54 cm/pulgada)
//        val dpi = 160.0  // DPI estándar
//        val pixelToCm = 2.54 / dpi  // Convertir píxeles a centímetros
//
//        for (block in visionText.textBlocks) {
//            val boundingBox = block.boundingBox
//            if (boundingBox != null) {
//                val width = boundingBox.width()?.toDouble() ?: 0.0  // Convertir a Double
//                val height = boundingBox.height()?.toDouble() ?: 0.0  // Convertir a Double
//                val widthInCm = width * pixelToCm
//                val heightInCm = height * pixelToCm
//                val blockSize = widthInCm * heightInCm
//
//                if (blockSize > 0.30) {  // Filtrar bloques mayores a 0.30 cm²
//                    if (blockSize > maxSize) {
//                        largestTextBlocks.clear()
//                        maxSize = blockSize
//                        largestTextBlocks.add(block)
//                    } else if (blockSize == maxSize) {
//                        largestTextBlocks.add(block)
//                    }
//                }
//            }
//        }
//
//        return largestTextBlocks
//    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
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


    // Permisos

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity(), it) == PackageManager.PERMISSION_GRANTED
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

