package com.blanco.somelai.ui.home.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blanco.somelai.R
import com.blanco.somelai.data.firebase.authentification.EmailAndPasswordAuthenticationManager
import com.blanco.somelai.data.firebase.realtime_database.RealTimeDatabaseManager
import com.blanco.somelai.data.firebase.realtime_database.model.UserData
import com.blanco.somelai.data.storage.DataStoreManager
import com.blanco.somelai.databinding.FragmentProfileBinding
import com.blanco.somelai.ui.login.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class ProfileFragment : Fragment() {

    private lateinit var _binding: FragmentProfileBinding
    private val binding: FragmentProfileBinding get() = _binding


    private lateinit var realTimeDatabaseManager: RealTimeDatabaseManager
    private lateinit var auth: FirebaseAuth

    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    // leemos los datos del usuario en fireBase
    private fun getUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            lifecycleScope.launch(Dispatchers.IO) {
                val userData = realTimeDatabaseManager.readUser(uid)
                withContext(Dispatchers.Main) {
                    if (userData != null) {
                        setUserData(userData)
                    } else {
                        // TODO  Manejar el caso en que no se encuentren datos del usuario
                        showMessage("No se encontraron datos del usuario.")
                    }
                }
            }
        } else {
            // TODO Manejar el caso en que no haya un usuario autenticado
            showMessage("No hay un usuario autenticado.")
        }
    }

    // TODO agregar funcion para que pinte la foto usuario
    private fun setUserData(user: UserData) {
        binding.tvMail.text = user.userEmail // Asegúrate de que UserData tenga un campo para el email
        binding.tvUserName.text = user.userName // Asegúrate de que UserData tenga un campo para el nombre de usuario
    }


    // Borramos la cuenta de Database
    private fun deleteAccount() {
        try{
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val uid = currentUser.uid
                lifecycleScope.launch(Dispatchers.IO) {
                    realTimeDatabaseManager.deleteUser(uid)
                    showMessage("Lamentamos que nos dejes. Puedes volver siempre que quieras")
                }
                    //Retrocedemos a la pantalla anterior
                    deleteUserStoredData()
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
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            currentUser.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Usuario", "Cuenta eliminada exitosamente")
                        // Aquí puedes redirigir al usuario a la pantalla de inicio de sesión o mostrar un mensaje
                    } else {
                        Log.e("Usuario", "Error al eliminar la cuenta", task.exception)
                        // Manejar el error
                    }
                }
        } else {
            Log.e("Usuario", "No hay un usuario autenticado")
            // Manejar el caso en que no haya un usuario autenticado
        }
    }

    //region --- DataStore ---

    // Borramos de dataStore
    private fun deleteUserStoredData() {
        lifecycleScope.launch(Dispatchers.IO) {
            dataStoreManager.logOut()
        }
    }
    //endregion --- DataStore ---

    //region --- Firebase Auth ---

    private fun logoutFirebase() {
        val manager = EmailAndPasswordAuthenticationManager()
        manager.signOut()
    }
    //endregion --- Firebase Auth ---

    //region --- Messages ---
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
    //region --- Messages ---

    //region --- Other ---
    private fun logOut() {
        lifecycleScope.launch(Dispatchers.IO) {
            logoutFirebase()
            deleteUserStoredData()
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
    //endregion --- Other ---


}