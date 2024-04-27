package com.blanco.somelai.ui.login

import SignUpFragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.blanco.somelai.R
import com.blanco.somelai.data.firebase.authentification.EmailAndPasswordAuthenticationManager
import com.blanco.somelai.data.storage.DataStoreManager
import com.blanco.somelai.databinding.FragmentLoginBinding
import com.blanco.somelai.ui.home.HomeActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginFragment : BottomSheetDialogFragment() {

    private lateinit var _binding: FragmentLoginBinding
    private val binding: FragmentLoginBinding get() = _binding
    val firebaseAuth = EmailAndPasswordAuthenticationManager()
    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataStoreManager = DataStoreManager(requireContext())
        setClicks()
    }

    private fun cleanData(){
        binding.etLoginEmail.setText("")
        binding.etLoginPassword.setText("")
    }

    private fun isDataValid(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}".toRegex()
        val passwordPattern = ".{6,}".toRegex()
        val email = binding.etLoginEmail.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()
        return email.matches(emailPattern) && email.isNotEmpty() &&
                password.matches(passwordPattern) && password.isNotEmpty()
    }

    private fun setClicks() {
        binding.btnLogin.setOnClickListener {
            if (isDataValid()) {
                val email = binding.etLoginEmail.text.toString().trim()
                val password = binding.etLoginPassword.text.toString().trim()
                sendLogin(email, password)
            } else {
                showEmptyError()
            }
        }
        binding.btnLoginGoToSignUp.setOnClickListener {
            navigateToSignUp()
            cleanData()
        }
    }

    private fun navigateToSignUp() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.setReorderingAllowed(true)
            .replace(R.id.fcv_login, SignUpFragment())
            .addToBackStack(null)
            .commit()
    }


    private fun sendLogin(email: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val resultIsSuccessful =
                firebaseAuth.signInFirebaseEmailAndPassword(email, password)
            if (resultIsSuccessful) {
                dataStoreManager.saveUser(email, password)
                navigateToHome()
            } else {
                // TODO Manejar que pasa cuando el useuario pone una contraseña incorrecta
                showInvalidCredentialsMessage()
            }
        }
    }

   private fun navigateToHome() {
        val intent = Intent(requireContext(), HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showEmptyError() {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), R.string.log_in_empty_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showInvalidCredentialsMessage() {
        activity?.runOnUiThread {
            Toast.makeText(
                requireContext(),
                R.string.log_in_invalid_credentials,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}