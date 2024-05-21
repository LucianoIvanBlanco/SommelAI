package com.blanco.somelai.ui.home.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blanco.somelai.R
import com.blanco.somelai.data.firebase.cloud_storage.CloudStorageManager
import com.blanco.somelai.data.firebase.realtime_database.RealTimeDatabaseManager
import com.blanco.somelai.data.firebase.realtime_database.model.UserData
import com.blanco.somelai.data.storage.DataStoreManager
import com.blanco.somelai.databinding.FragmentEditProfileBinding
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileFragment : Fragment() {

    private lateinit var _binding: FragmentEditProfileBinding
    private val binding: FragmentEditProfileBinding get() = _binding

    private lateinit var realTimeDatabaseManager: RealTimeDatabaseManager
    private lateinit var auth: FirebaseAuth
    private lateinit var dataStoreManager: DataStoreManager

    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String? = null


    private lateinit var cloudStorageManager: CloudStorageManager
    private lateinit var profileImageButton: ImageButton


    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                showDeniedPermissionMessage()
            }
        }

    private var imageGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    uploadImage(uri)
                } ?: run {
                    showErrorMessageNoImage()
                }
            } else {
                showErrorMessageNoImage()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataStoreManager = DataStoreManager(requireContext())
        cloudStorageManager = CloudStorageManager()
        realTimeDatabaseManager = RealTimeDatabaseManager()
        auth = FirebaseAuth.getInstance()
        profileImageButton = view.findViewById(R.id.img_btn_profile)

        loadUserProfilePicture()
        setClicks()
        getUserData()
    }

    //region --- UI Related ---

    private fun setClicks() {
        binding.btnSaveChanges.setOnClickListener {
            if (isDataValid()) {
                if (uploadedImageUrl != null) {
                    updateUserProfile()
                    showMessage("Datos actualizados.")
                } else {
                    showMessage("No se ha subido ninguna imagen.")
                }
                findNavController().popBackStack()
            } else {
                showMessage("Error, debes completar todos los campos.")
            }
        }

        binding.imgBtnProfile.setOnClickListener {
            if (uploadedImageUrl != null) {
                deleteImageAndOpenGallery()
            } else {
                checkIfWeAlreadyHaveThisPermission()
            }
        }
    }

    private fun loadUserProfilePicture() {
        lifecycleScope.launch {
            val userPhotoUrl = cloudStorageManager.getUserProfilePicture()
            if (userPhotoUrl != null) {
                Glide.with(requireContext())
                    .asBitmap()
                    .load(userPhotoUrl)
                    .into(profileImageButton)
            }
        }
    }

    private fun setImagePreview(uploadedImageResponse: String) {

        Glide.with(requireContext())
            .asBitmap()
            .load(uploadedImageResponse)
            .centerCrop()
            .into(profileImageButton)
    }

    //endregion --- UI Related ---

    //region --- Firebase - CloudStorage ---

    private fun uploadImage(selectedImageUri: Uri?) {
        lifecycleScope.launch(Dispatchers.IO) {
            val uploadedImageResponse = cloudStorageManager.uploadProfileImage(selectedImageUri!!)
            withContext(Dispatchers.Main) {
                if (uploadedImageResponse != null) {
                    uploadedImageUrl = uploadedImageResponse
                    setImagePreview(uploadedImageResponse)
                }
            }
        }
    }

    private fun deleteImageAndOpenGallery() {
        lifecycleScope.launch(Dispatchers.IO) {
            if (uploadedImageUrl != null) {
                //Esperamos a que nos retorne si la foto se borró a partir del enlace o no
                val wasDeleted: Boolean = CloudStorageManager().deleteImage(uploadedImageUrl!!)
                if (wasDeleted) {
                    Log.i("EditProfileFragment", "Foto eliminada")
                }
                openGallery()
            }
        }
    }
    //endregion --- Firebase - CloudStorage ---

    private fun isDataValid(): Boolean {
        val userNamePattern = "^[a-zA-Z0-9]{4,}$".toRegex()
        val fullNamePattern = "^[a-zA-Z]{3,20}\\s[a-zA-Z]{4,20}$".toRegex()
        val passwordPattern = ".{6,10}".toRegex()
        val password = binding.etProfilePassword.text.toString().trim()
        val userName = binding.etProfileUserName.text.toString().trim()
        val fullName = binding.etProfileFullName.text.toString().trim()

        return fullName.matches(fullNamePattern) && fullName.isNotEmpty() &&
                password.matches(passwordPattern) && password.isNotEmpty() &&
                userName.matches(userNamePattern) && userName.isNotEmpty()
    }

    // TODO agregamos opcion de tomar fotografia?
    fun updateUserProfile() {
        val userName = binding.etProfileUserName.text.toString()
        val userFullName = binding.etProfileFullName.text.toString()
        val userPassword = binding.etProfilePassword.text.toString()
        val userUid = auth.currentUser?.uid.toString()
        val userEmail = auth.currentUser?.email.toString()
        val userPhotoUrl = uploadedImageUrl!!

        // Crea un objeto UserData con los datos recopilados
        val userData = UserData(
            uid = userUid,
            userEmail = userEmail,
            userName = userName,
            userFullName = userFullName,
            userPassword = userPassword,
            userPhotoUrl = userPhotoUrl
        )
        // Llama a la función updateUser del RealTimeDatabaseManager para actualizar los datos del usuario
        realTimeDatabaseManager.updateUser(userData)
    }

    private fun getUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid

            lifecycleScope.launch(Dispatchers.IO) {
                val userData = realTimeDatabaseManager.readUser(uid)
                withContext(Dispatchers.Main) {

                    if (userData != null) {
                        setOldUserData(userData)
                    } else {
                        showMessage("No se encontraron datos del usuario.")
                    }
                }
            }
        } else {
            showMessage("No hay usuario autentificado.")
        }
    }

    private fun setOldUserData(user: UserData) {
        binding.etProfileUserName.setText(user.userName)
        binding.etProfileFullName.setText(user.userFullName)
        binding.etProfilePassword.setText(user.userPassword)

        Glide.with(requireContext())
            .asBitmap()
            .load(user.userPhotoUrl)
            .placeholder(R.drawable.default_user)
            .fallback(R.drawable.default_user)
            .into(profileImageButton)
    }


    private fun showMessage(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }


    //region --- Messages ---

    private fun showPermissionRationaleDialog(externalStoragePermission: String) {

        val title = getString(R.string.new_advertisement_permission_dialog_title)
        val message = getString(R.string.new_advertisement_permission_dialog_message)
        val positiveButton = getString(R.string.new_advertisement_permission_dialog_positive_button)
        val negativeButton = getString(R.string.new_advertisement_permission_dialog_negative_button)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { dialog, which ->
                requestPermissionLauncher.launch(externalStoragePermission)
            }
            .setNegativeButton(negativeButton) { dialog, which -> requireActivity().finish() }
            .show()
    }

    private fun showDeniedPermissionMessage() {
        val message = getString(R.string.new_advertisement_denied_permission)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showErrorMessageNoImage() {
        val message = getString(R.string.new_advertisement_no_select_image_error)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    //endregion --- Messages ---


    //region --- Photo gallery ---
    private fun checkIfWeAlreadyHaveThisPermission() {
        val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val permissionStatus =
            ContextCompat.checkSelfPermission(requireContext(), permissionToRequest)

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            val shouldRequestPermission = shouldShowRequestPermissionRationale(permissionToRequest)
            if (shouldRequestPermission) {
                showPermissionRationaleDialog(permissionToRequest)
            } else {
                requestPermissionLauncher.launch(permissionToRequest)
            }
        }
    }


    private fun openGallery() {
        Log.d("EditProfileFragment", "Attempting to open gallery")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        try {
            imageGalleryLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("EditProfileFragment", "Failed to open gallery", e)
        }
    }
    //endregion --- Photo gallery ---


    //region --- Others: data validations, strings... ---

    // TODO podemos usar para avisar de campos vacios al usuario

    private fun checkData(title: String, price: Double, description: String): Boolean {
        var isDataValid = true
        if (title.isNullOrEmpty()) {
            isDataValid = false
            showMessage("Debes poner un título al anuncio")
        } else if (price <= 0.0) {
            isDataValid = false
            showMessage("Necesitas poner un precio al artículo")
        } else if (description.isNullOrEmpty()) {
            isDataValid = false
            showMessage("Debes poner una descripción al anuncio")
        } else if (uploadedImageUrl == null) {
            isDataValid = false
            showMessage("Necesitas subir una foto para el anuncio")
        }
        return isDataValid
    }
    //endregion --- Others: data validations, strings... ---
}
