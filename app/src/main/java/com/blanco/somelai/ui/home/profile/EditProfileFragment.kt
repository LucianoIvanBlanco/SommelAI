package com.blanco.somelai.ui.home.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
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
import java.io.ByteArrayOutputStream
import java.io.InputStream

class EditProfileFragment : Fragment() {

    private lateinit var _binding: FragmentEditProfileBinding
    private val binding: FragmentEditProfileBinding get() = _binding

    private lateinit var realTimeDatabaseManager: RealTimeDatabaseManager
    private lateinit var auth: FirebaseAuth
    private lateinit var dataStoreManager: DataStoreManager

    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String? = null
    private var tempBase64Image: String? = null


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
                    handleImageUri(uri)
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

    private fun setClicks() {
        binding.btnSaveChanges.setOnClickListener {
            handleSaveChanges()
        }

        binding.imgBtnProfile.setOnClickListener {
            if (uploadedImageUrl != null) {
                deleteImageAndOpenGallery()
            } else {
                checkIfWeAlreadyHaveThisPermission()
            }
        }
    }

    private fun handleSaveChanges() {
        Log.d("EditProfileFragment", "handleSaveChanges: Start")
        if (isDataValid()) {
            Log.d("EditProfileFragment", "handleSaveChanges: Data is valid")
            if (uploadedImageUrl != null) {
                Log.d("EditProfileFragment", "handleSaveChanges: Image has been uploaded")
                val newUserName = binding.etProfileUserName.text.toString().trim()
                val newFullName = binding.etProfileFullName.text.toString().trim()
                val newPassword = binding.etProfilePassword.text.toString().trim()
                val newImageUrl = uploadedImageUrl!!
                Log.d("EditProfileFragment", "handleSaveChanges: newImageUrl: $newImageUrl")
                updateUserProfile(newUserName, newFullName, newPassword, newImageUrl) {
                    setImagePreview(newImageUrl) // Asegúrate de actualizar la vista con la nueva imagen
                    showMessage(getString(R.string.show_message_update_image))
                    findNavController().popBackStack()
                }
            } else {
                Log.d("EditProfileFragment", "handleSaveChanges: Image has not been uploaded")
                // Obtener los datos actuales del usuario
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val uid = currentUser.uid
                    Log.d("EditProfileFragment", "handleSaveChanges: Current user UID: $uid")
                    lifecycleScope.launch(Dispatchers.IO) {
                        val userData = realTimeDatabaseManager.readUser(uid)
                        withContext(Dispatchers.Main) {
                            if (userData != null) {
                                Log.d("EditProfileFragment", "handleSaveChanges: User data retrieved")
                                // Verificar si los datos han sido actualizados
                                val newUserName = binding.etProfileUserName.text.toString().trim()
                                val newFullName = binding.etProfileFullName.text.toString().trim()
                                val newPassword = binding.etProfilePassword.text.toString().trim()

                                Log.d("EditProfileFragment", "handleSaveChanges: New UserName: $newUserName")
                                Log.d("EditProfileFragment", "handleSaveChanges: New FullName: $newFullName")
                                Log.d("EditProfileFragment", "handleSaveChanges: New Password: $newPassword")

                                val isUserNameUpdated = newUserName != userData.userName
                                val isFullNameUpdated = newFullName != userData.userFullName
                                val isPasswordUpdated = newPassword.isNotEmpty() && newPassword != userData.userPassword

                                Log.d("EditProfileFragment", "handleSaveChanges: isUserNameUpdated: $isUserNameUpdated")
                                Log.d("EditProfileFragment", "handleSaveChanges: isFullNameUpdated: $isFullNameUpdated")
                                Log.d("EditProfileFragment", "handleSaveChanges: isPasswordUpdated: $isPasswordUpdated")

                                if (isUserNameUpdated || isFullNameUpdated || isPasswordUpdated) {
                                    Log.d("EditProfileFragment", "handleSaveChanges: Data has been updated")
                                    updateUserProfile(newUserName, newFullName, newPassword, uploadedImageUrl) {
                                        showMessage(getString(R.string.show_message_data_updated))
                                        findNavController().popBackStack()
                                    }
                                } else {
                                    Log.d("EditProfileFragment", "handleSaveChanges: No changes detected")
                                    showMessage(getString(R.string.show_message_no_changes_detected))
                                    navigateToPreviousFragment()
                                }
                            } else {
                                Log.d("EditProfileFragment", "handleSaveChanges: User data is null")
                            }
                        }
                    }
                } else {
                    Log.d("EditProfileFragment", "handleSaveChanges: Current user is null")
                }
            }
        } else {
            Log.d("EditProfileFragment", "handleSaveChanges: Data is not valid")
            showMessage(getString(R.string.show_message_error_complete_all))
        }
        Log.d("EditProfileFragment", "handleSaveChanges: End")
    }

    private fun navigateToPreviousFragment() {
        findNavController().popBackStack()
    }

    private fun loadUserProfilePicture() {
        lifecycleScope.launch(Dispatchers.IO) {
            val userPhotoUrl = cloudStorageManager.getUserProfilePicture()
            withContext(Dispatchers.Main) {
                if (userPhotoUrl != null) {
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(userPhotoUrl)
                        .into(profileImageButton)
                }
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
                val wasDeleted: Boolean = CloudStorageManager().deleteImage(uploadedImageUrl!!)
                if (wasDeleted) {
                    Log.i("EditProfileFragment", "Foto eliminada")
                }
                openGallery()
            }
        }
    }

    private fun isDataValid(): Boolean {
        val userNamePattern = "^[a-zA-Z0-9]{3,}$".toRegex()
        val fullNamePattern = "^[a-zA-Z]{3,20}(\\s[a-zA-Z]{3,20}){1,5}$".toRegex()
        val passwordPattern = ".{6,10}".toRegex()
        val password = binding.etProfilePassword.text.toString().trim()
        val repeatPassword = binding.etRepeatPassword.text.toString().trim()
        val userName = binding.etProfileUserName.text.toString().trim()
        val fullName = binding.etProfileFullName.text.toString().trim()

        if (!userName.matches(userNamePattern) || userName.isEmpty()) {
            return false
        }

        if (!fullName.matches(fullNamePattern) || fullName.isEmpty()) {
            return false
        }

        if (!password.matches(passwordPattern) || password.isEmpty()) {
            return false
        }
        if (!repeatPassword.matches(passwordPattern) || repeatPassword.isEmpty()) {
            return false
        }
        if (password != repeatPassword) {
            showPasswordMismatchMessage()
            return false
        }
        return true
    }

    private fun updateUserProfile(newUserName: String, newFullName: String, newPassword: String, newImageUrl: String?, onComplete: () -> Unit) {
        val userUid = auth.currentUser?.uid.toString()
        val userEmail = auth.currentUser?.email.toString()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userData = realTimeDatabaseManager.readUser(userUid)
                if (userData != null) {
                    val updatedUserData = userData.copy(
                        userName = newUserName,
                        userFullName = newFullName,
                        userPassword = newPassword,
                        userPhotoUrl = newImageUrl ?: userData.userPhotoUrl // Mantener la URL actual si newImageUrl es null
                    )

                    realTimeDatabaseManager.updateUser(updatedUserData)
                    updateUserDataInDataStore(userEmail, newPassword, userUid, newFullName, newUserName)

                    withContext(Dispatchers.Main) {
                        onComplete()
                    }
                } else {
                    Log.e("EditProfileFragment", "updateUserProfile: User data is null")
                }
            } catch (e: Exception) {
                Log.e("EditProfileFragment", "updateUserProfile: Error updating user data", e)
                withContext(Dispatchers.Main) {
                    showMessage(getString(R.string.show_message_error_update_data))
                }
            }
        }
    }

    private suspend fun updateUserDataInDataStore(email: String, password: String, id: String, fullName: String, userName: String) {
        dataStoreManager.saveUserData(email, password, id, fullName, userName)
        tempBase64Image?.let {
            dataStoreManager.saveUserPhoto(it)
        }
    }

    private fun handleImageUri(imageUri: Uri?) {
        val base64Image = convertImageToBase64(requireContext(), imageUri)
        if (base64Image != null) {
            tempBase64Image = base64Image
        } else {
            Log.e("EditProfileFragment", "La imagen en Base64 es nula, no se puede convertir.")
        }
    }

    private fun convertImageToBase64(context: Context, imageUri: Uri?): String? {
        return try {
            val inputStream: InputStream? = imageUri?.let {
                context.contentResolver.openInputStream(it)
            }
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                Base64.encodeToString(byteArray, Base64.DEFAULT)
            } else {
                Log.e("EditProfileFragment", "Error al abrir el InputStream de la imagen: No se pudo abrir el archivo de imagen desde el URI proporcionado.")
                null
            }
        } catch (e: Exception) {
            Log.e("EditProfileFragment", "Error al convertir la imagen a Base64: ${e.message}")
            null
        }
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
                        showMessage(getString(R.string.show_message_error_not_found_data))
                    }
                }
            }
        } else {
            showMessage(getString(R.string.show_message_error_not_found_user_login))
        }
    }

    private fun setOldUserData(user: UserData) {
        binding.etProfileUserName.setText(user.userName)
        binding.etProfileFullName.setText(user.userFullName)

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

    private fun showPermissionRationaleDialog(externalStoragePermission: String) {

        val title = getString(R.string.show_permission_necessary)
        val message = getString(R.string.show_permission_necessary_photo)
        val positiveButton = getString(R.string.show_permission_dialog_positive_button)
        val negativeButton = getString(R.string.show_permission_dialog_negative_button)

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
        val message = getString(R.string.show_message_denied_permission)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showErrorMessageNoImage() {
        val message = getString(R.string.show_message_no_select_image_error)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showPasswordMismatchMessage() {
        Toast.makeText(requireContext(), R.string.sign_up_password_mismatch_error, Toast.LENGTH_SHORT).show()
    }

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
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        try {
            imageGalleryLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("EditProfileFragment", "Failed to open gallery", e)
        }
    }
}
