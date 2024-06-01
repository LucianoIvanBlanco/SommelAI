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
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.blanco.somelai.R
import com.blanco.somelai.databinding.FragmentScannerCameraBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerCameraFragment : Fragment() {

    private lateinit var _binding: FragmentScannerCameraBinding
    private val binding get() = _binding

    private val viewModel: WineViewModel by activityViewModels()
    private lateinit var progressIndicator: CircularProgressIndicator

    // Camera X
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null

    private val requestPermissionCameraLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                showDeniedPermissionMessage()
                requireActivity().finish()
            }
        }

    private val requestPermissionGaleryLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pickImageFromGallery()
            } else {
                showDeniedPermissionMessage()
            }
        }

    companion object {
        private const val TAG = "SommelAI"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScannerCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkIfWeAlreadyHaveThisPermission()
        initializeFlashButtonIcon()
        setClicks()
        cameraExecutor = Executors.newSingleThreadExecutor()

        progressIndicator = view.findViewById(R.id.progress_circular)

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        hideLoadingSpinner()
    }

    private fun observeViewModel() {
        viewModel.navigateToWineList.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                hideLoadingSpinner()
                findNavController().navigate(R.id.action_scannerCameraFragment_to_wineListFragment)
                viewModel.resetNavigateToWineList()
            }
        })
        viewModel.navigateToWineFeed.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                hideLoadingSpinner()
                viewModel.resetNavigateToFeedFragment()
                findNavController().navigate(R.id.action_scannerCameraFragment_to_feedFragment)
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

        binding.addPhotoButton.setOnClickListener {
            checkGalleryPermissionAndPickImage()
        }
    }

    private fun checkGalleryPermissionAndPickImage() {
        val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val permissionStatus = ContextCompat.checkSelfPermission(requireContext(), permissionToRequest)

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            pickImageFromGallery()
        } else {
            val shouldRequestPermission = shouldShowRequestPermissionRationale(permissionToRequest)
            if (shouldRequestPermission) {
                showPermissionRationaleDialog(permissionToRequest)
            } else {
                requestPermissionGaleryLauncher.launch(permissionToRequest)
            }
        }
    }

    private val pickImageLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                showImageConfirmationDialog(it)
            }
        }

    private fun showImageConfirmationDialog(uri: Uri) {
        val bitmap = uriToBitmap(uri)
        val rotatedBitmap = rotateBitmapIfNeeded(bitmap)
        val imageView = ImageView(requireContext()).apply {
            setImageBitmap(rotatedBitmap)
            adjustViewBounds = true
        }

        // TODO meter en strings
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.show_select_photo))
            .setMessage(getString(R.string.show_select_photo_advertisement))
            .setView(imageView)
            .setPositiveButton(getString(R.string.show_permission_dialog_positive_button)) { dialog, which ->
                showLoadingSpinner()
                viewModel.getWinesAndFilterByName(uri, requireContext())
                Toast.makeText(context, getString(R.string.show_start_search_wine), Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(getString(R.string.show_message_button_other_photo)) { dialog, which ->
                pickImageFromGallery()
            }
            .show()
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap?): Bitmap? {
        return bitmap?.let {
            if (it.width > it.height) {
                rotateBitmap(it, 90f)
            } else {
                it
            }
        }
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
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
                        showLoadingSpinner()
                        viewModel.getWinesAndFilterByName(uri, requireContext())
                        Toast.makeText(context, getString(R.string.show_start_search_wine), Toast.LENGTH_LONG).show()

                    }
                }
            }
        )
    }

    private fun showLoadingSpinner() {
        progressIndicator.visibility = View.VISIBLE
    }

    private fun hideLoadingSpinner() {
        progressIndicator.visibility = View.GONE
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Obtener la orientación de la imagen
        val exif = requireActivity().contentResolver.openInputStream(uri)?.let { ExifInterface(it) }
        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        Log.d("ScannerCameraFragment", "Image orientation: $orientation")

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap?, degrees: Float): Bitmap? {
        if (bitmap == null) return null
        val matrix = android.graphics.Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    private fun checkIfWeAlreadyHaveThisPermission() {
        val cameraPermission: String = Manifest.permission.CAMERA
        val permissionStatus =
            ContextCompat.checkSelfPermission(requireContext(), cameraPermission)

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            val shouldRequestPermission =
                shouldShowRequestPermissionRationale(cameraPermission)

            if (shouldRequestPermission) {
                showPermissionRationaleDialog(cameraPermission)
            } else {
                requestPermissionCameraLauncher.launch(cameraPermission)
            }
        }
    }

    private fun showPermissionRationaleDialog(cameraPermission: String) {
        val title = getString(R.string.show_permission_necessary)
        val message = getString(R.string.show_permission_necessary_camera)
        val positiveButton = getString(R.string.show_permission_dialog_positive_button)
        val negativeButton = getString(R.string.show_permission_dialog_negative_button)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { dialog, which ->
                requestPermissionCameraLauncher.launch(cameraPermission)
            }
            .setNegativeButton(negativeButton) { dialog, which -> requireActivity().finish() }
            .show()
    }

    private fun showDeniedPermissionMessage() {
        val message = getString(R.string.show_message_denied_permission)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

}
