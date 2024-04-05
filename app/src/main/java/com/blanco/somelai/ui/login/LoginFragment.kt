package com.blanco.somelai.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.blanco.somelai.R
import com.blanco.somelai.databinding.FragmentLoginBinding
import com.blanco.somelai.ui.home.HomeActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LoginFragment : BottomSheetDialogFragment() {

    private lateinit var _binding: FragmentLoginBinding
    private val binding: FragmentLoginBinding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClicks()
    }

    private fun isDataValid(): Boolean {
        val email = binding.etLoginEmail.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()
        return email.isNotEmpty() && password.isNotEmpty()
    }

    private fun setClicks() {
        binding.btnLogin.setOnClickListener {
            if (isDataValid()) {
                sendLogin()
            } else {
                showEmptyError()
            }
        }
        binding.btnLoginGoToSignUp.setOnClickListener { navigateToSignUp() }
    }

    private fun navigateToSignUp() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.setReorderingAllowed(true)
            .replace(R.id.fcv_login, SignUpFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showEmptyError() {
        val message = R.string.error_message_empty_fields
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun sendLogin() {
        //TODO hacer la petición al servidor
        //Actualizar info

        navigateToHome()
    }

    private fun navigateToHome() {
        val intent = Intent(requireContext(), HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }


}