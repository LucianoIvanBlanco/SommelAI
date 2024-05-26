package com.blanco.somelai.ui.home.profile

import android.content.Intent
import android.os.Bundle
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
import com.blanco.somelai.data.firebase.realtime_database.model.UserData
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
        getUserData()
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

    private fun getUserData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUser =
                    auth.currentUser ?: throw IllegalStateException("No hay usuario autentificado.")
                val uid = currentUser.uid
                val userData = realTimeDatabaseManager.readUser(uid)
                withContext(Dispatchers.Main) {
                    if (userData != null) {
                        setUserData(userData)
                    } else {
                        showMessage("No se encontraron datos del usuario.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showMessage("Error al obtener los datos del usuario: ${e.message}")
                }
            }
        }
    }

    private fun setUserData(user: UserData) {
        binding.tvMail.text = user.userEmail
        binding.tvUserName.text = user.userName

        // TODO la imagen carga lento
        Glide.with(requireContext())
            .asBitmap()
            .centerCrop()
            .load(user.userPhotoUrl)
            .placeholder(R.drawable.default_user) // Imagen por defecto mientras se carga
            .error(R.drawable.default_user)  // Imagen por defecto si hay un error
            .into(binding.ivProfile)
    }

    // Borramos la cuenta de Database
    private fun deleteAccount() {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val uid = currentUser.uid
                lifecycleScope.launch(Dispatchers.IO) {
                    realTimeDatabaseManager.deleteUser(uid)
                    showMessage("Lamentamos que nos dejes. Puedes volver siempre que quieras")
                }
                logOutStoredData()
                goLogin()
                deleteUserAccount()

            } else {
                showMessage("Error al eliminar tu cuenta")
            }
        } catch (e: Exception) {
            Log.e("deleteAccount", "$e")
            showMessage(e.localizedMessage)
        }
    }

    // Borramos de firebaseAuth
    private fun deleteUserAccount() {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser ?: throw IllegalStateException(
                "No hay un usuario autenticado"
            )
            currentUser.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("ProfileFragment", "Cuenta eliminada exitosamente")
                    } else {
                        Log.e("ProfileFragment", "Error al eliminar la cuenta", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error al intentar eliminar la cuenta: ${e.message}")
        }
    }

    // LogOut de dataStore
    //Todo falta eliminar la cuenta
    private fun logOutStoredData() {
        lifecycleScope.launch(Dispatchers.IO) {
            dataStoreManager.logOut()
        }
    }

    private fun logoutFirebase() {
        val manager = EmailAndPasswordAuthenticationManager()
        manager.signOut()
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
}