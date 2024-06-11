package com.blanco.somelai.ui.home.profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blanco.somelai.R
import com.blanco.somelai.data.firebase.authentification.EmailAndPasswordAuthenticationManager
import com.blanco.somelai.data.firebase.cloud_storage.CloudStorageManager
import com.blanco.somelai.data.firebase.realtime_database.RealTimeDatabaseManager
import com.blanco.somelai.data.storage.DataStoreManager
import com.blanco.somelai.databinding.FragmentProfileBinding
import com.blanco.somelai.ui.login.MainActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileFragment : Fragment() {

    private lateinit var _binding: FragmentProfileBinding
    private val binding: FragmentProfileBinding get() = _binding

    private lateinit var realTimeDatabaseManager: RealTimeDatabaseManager
    private lateinit var auth: FirebaseAuth

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var cloudStorageManager: CloudStorageManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cloudStorageManager = CloudStorageManager()
        dataStoreManager = DataStoreManager(requireContext())
        realTimeDatabaseManager = RealTimeDatabaseManager()
        auth = FirebaseAuth.getInstance()
        setClicks()
        //getUserData()              // Para cargar desde Firebase
        loadUserDataFromDataStore() // Cargar datos del usuario desde DataStore
    }

    private fun setClicks() {
        binding.btnDeleteAccount.setOnClickListener {
            showDialogDeleteAccount()
        }
        binding.btnLogOut.setOnClickListener {
            logOut()
        }
        binding.btnEditProfile.setOnClickListener {
            navigateToEditProfile()
        }
    }

    private fun navigateToEditProfile() {
        findNavController().navigate(R.id.editProfileFragment)
    }


    private fun loadUserDataFromDataStore() {
        lifecycleScope.launch(Dispatchers.IO) {
            val userDataFlow = dataStoreManager.getUserData()
            val userData = userDataFlow.first()  // Recolecta el primer valor del Flow

            withContext(Dispatchers.Main) {
                if (userData.isNotEmpty()) {
                    Log.d("ProfileFragment", "Datos del usuario: ${userData["email"]}, ${userData["userName"]}")
                    binding.tvMail.text = userData["email"]
                    binding.tvUserName.text = userData["userName"]

                    val photoBase64 = userData["photo"] ?: ""

                    val bitmap = convertBase64ToBitmap(photoBase64)
                    val rotatedBitmap = rotateBitmapIfNeeded(bitmap)
                    if (rotatedBitmap != null) {
                        Glide.with(requireContext())
                            .load(rotatedBitmap)
                            .centerCrop()
                            .placeholder(R.drawable.default_user)
                            .error(R.drawable.default_user)
                            .into(binding.ivProfile)
                    } else {
                        Log.e("ProfileFragment", "Bitmap es nulo, cargando imagen por defecto")
                        Glide.with(requireContext())
                            .load(R.drawable.default_user)
                            .into(binding.ivProfile)
                    }
                } else {
                    showMessage(getString(R.string.show_message_error_not_found_data))
                }
            }
        }
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

    private fun rotateBitmap(bitmap: Bitmap?, degrees: Float): Bitmap? {
        if (bitmap == null) return null
        val matrix = android.graphics.Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    fun convertBase64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedString = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: IllegalArgumentException) {
            Log.e("ProfileFragment", "Error al convertir Base64 a Bitmap: ${e.message}")
            null
        }
    }

    private fun deleteAccount() {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val uid = currentUser.uid
                lifecycleScope.launch(Dispatchers.IO) {
                    realTimeDatabaseManager.deleteUser(uid)
                    dataStoreManager.deleteUserData()

                    val authManager = EmailAndPasswordAuthenticationManager()
                    val result = authManager.deleteUserAccount()
                    if (result) {
                        Log.d("ProfileFragment", "Cuenta eliminada exitosamente")
                        withContext(Dispatchers.Main) {
                            showMessage(getString(R.string.account_deleted_message))
                            goLogin()
                        }
                    } else {
                        Log.e("ProfileFragment", "Error al eliminar la cuenta")
                        withContext(Dispatchers.Main) {
                            showMessage(getString(R.string.account_delete_failed_message))
                        }
                    }
                }
            } else {
                showMessage(getString(R.string.account_delete_failed_message))
            }
        } catch (e: Exception) {
            Log.e("deleteAccount", "$e")
            showMessage(e.localizedMessage)
        }
    }

    private fun logOut() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                // Log out from Firebase
                withContext(Dispatchers.IO) {
                    val manager = EmailAndPasswordAuthenticationManager()
                    manager.signOut()
                    logOutStoredData()
                }

                // Navigate to login
                goLogin()
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error during logout: ${e.message}")
            }
        }
    }

    private suspend fun logOutStoredData() {
        withContext(Dispatchers.IO) {
            dataStoreManager.logOut()
        }
    }

    private fun goLogin() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }


    private fun showDialogDeleteAccount() {
        val title = getString(R.string.account_delete_dialog_title)
        val message = getString(R.string.account_delete_dialog_message)
        val positiveButton = getString(R.string.account_delete_positive_button)
        val negativeButton = getString(R.string.account_delete_dialog_negative_button)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { dialog, which ->
                deleteAccount()
            }
            .setNegativeButton(negativeButton) { dialog, which -> requireActivity().finish() }
            .show()
    }

    private fun showMessage(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
