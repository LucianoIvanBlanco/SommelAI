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


    // TODO la imagen se carga de costado y carga despues de editar en EditProfile, hay que moverse de fragment para que lo haga.
    private fun loadUserDataFromDataStore() {
        lifecycleScope.launch(Dispatchers.IO) {
            val userData = dataStoreManager.getUserData()
            withContext(Dispatchers.Main) {
                if (userData.isNotEmpty()) {
                    Log.d("ProfileFragment", "Datos del usuario: ${userData["email"]}, ${userData["userName"]}")
                    binding.tvMail.text = userData["email"]
                    binding.tvUserName.text = userData["userName"]

                    val photoBase64 = userData["photo"] ?: ""

                    val bitmap = convertBase64ToBitmap(photoBase64)
                    if (bitmap != null) {
                        Glide.with(requireContext())
                            .load(bitmap)
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
                    showMessage("No se encontraron datos del usuario en DataStore.")
                }
            }
        }
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
                    dataStoreManager.clearAllData() // Eliminar todos los datos de DataStore

                    val authManager = EmailAndPasswordAuthenticationManager()
                    val result = authManager.deleteUserAccount()
                    if (result) {
                        Log.d("ProfileFragment", "Cuenta eliminada exitosamente")
                        withContext(Dispatchers.Main) {
                            showMessage("Cuenta eliminada exitosamente")
                            goLogin()
                        }
                    } else {
                        Log.e("ProfileFragment", "Error al eliminar la cuenta")
                        withContext(Dispatchers.Main) {
                            showMessage("Error al eliminar la cuenta")
                        }
                    }
                }
            } else {
                showMessage("Error al eliminar tu cuenta")
            }
        } catch (e: Exception) {
            Log.e("deleteAccount", "$e")
            showMessage(e.localizedMessage)
        }
    }

    private fun logOutStoredData() {
        lifecycleScope.launch(Dispatchers.IO) {
            dataStoreManager.logOut()
        }
    }

    private fun logoutFirebase() {
        val manager = EmailAndPasswordAuthenticationManager()
        manager.signOut()
    }

    private fun logOut() {
        lifecycleScope.launch(Dispatchers.IO) {
            logoutFirebase()
            logOutStoredData()
            withContext(Dispatchers.Main) {
                goLogin()
            }
        }
    }

    private fun goLogin() {
        val intent = Intent(requireContext(), MainActivity::class.java)
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

//--------------------- CARGA DE DATOS DESDE FIREBASE ---------------------------------

//    private fun setUserData(user: UserData) {
//        binding.tvMail.text = user.userEmail
//        binding.tvUserName.text = user.userName
//
//        Glide.with(requireContext())
//            .asBitmap()
//            .centerCrop()
//            .load(user.userPhotoUrl)
//            .placeholder(R.drawable.default_user)
//            .error(R.drawable.default_user)
//            .diskCacheStrategy(DiskCacheStrategy.ALL)
//            .into(binding.ivProfile)
//    }

//    private fun getUserData() {
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                val currentUser =
//                    auth.currentUser ?: throw IllegalStateException("No hay usuario autentificado.")
//                val uid = currentUser.uid
//                val userData = realTimeDatabaseManager.readUser(uid)
//                withContext(Dispatchers.Main) {
//                    if (userData != null) {
//                        setUserData(userData)
//                    } else {
//                        showMessage("No se encontraron datos del usuario.")
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    showMessage("Error al obtener los datos del usuario: ${e.message}")
//                }
//            }
//        }
//    }
//-------------------------------------------------------------------------------------------------