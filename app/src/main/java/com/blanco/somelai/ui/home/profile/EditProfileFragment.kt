package com.blanco.somelai.ui.home.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.blanco.somelai.data.firebase.realtime_database.RealTimeDatabaseManager
import com.blanco.somelai.data.firebase.realtime_database.model.UserData
import com.blanco.somelai.data.storage.DataStoreManager
import com.blanco.somelai.databinding.FragmentEditProfileBinding
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
        realTimeDatabaseManager = RealTimeDatabaseManager()
        auth = FirebaseAuth.getInstance()

        setClicks()
        getUserData()
    }

    private fun setClicks() {
        binding.btnSaveChanges.setOnClickListener {
            if (isDataValid()) {
                updateUserProfile()
                showMessage("Datos actualizados")
            } else {
                showMessage("Error, debes completar todos los campos")
            }
        }

        binding.imgBtnProfile.setOnClickListener{

            // TODO logica para abrir galeria y seleccionar foto
            // Todo tambien tendriamos que poder visualzar la foto cargada

        }
    }


    private fun isDataValid(): Boolean {
        // Expresiones regulares para email y password
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

    fun updateUserProfile() {
        // Recopila los datos de los campos EditText
        val userName = binding.etProfileUserName.text.toString()
        val userFullName = binding.etProfileFullName.text.toString()
        val userPassword = binding.etProfilePassword.text.toString()
        // Asegúrate de que el usuario tenga una clave válida para actualizar su perfil
        val userKey =
            auth.currentUser?.uid.toString()// Aquí debes obtener la clave del usuario actual

        // Crea un objeto UserData con los datos recopilados
        val userData = UserData(
            key = userKey,
            userName = userName,
            userFullName = userFullName,
            userPassword = userPassword
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
            showMessage("No hay un usuario autenticado.")
        }
    }

    private fun setOldUserData(user: UserData) {
        binding.etProfileUserName.setText(user.userName)// Asegúrate de que UserData tenga un campo para el email
        binding.etProfileFullName.setText(user.userFullName)// Asegúrate de que UserData tenga un campo para el nombre de usuario
        binding.etProfilePassword.setText(user.userPassword)
    }

    private fun showMessage(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

}