package com.blanco.somelai.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blanco.somelai.databinding.ActivityMainBinding
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityMainBinding
    private val binding: ActivityMainBinding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
        checkLogin()
    }

    private fun isUserLogged(): Boolean {
        return true
    }

    private fun checkLogin() {
        if (isUserLogged()) {

        }
    }
}