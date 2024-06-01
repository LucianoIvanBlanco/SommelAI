package com.blanco.somelai.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blanco.somelai.R
import com.blanco.somelai.databinding.ActivityMainBinding
import com.blanco.somelai.ui.home.HomeActivity
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.auth.FirebaseAuth
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
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser != null
    }

    private fun checkLogin() {
        if (isUserLogged()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fcv_login, LoginFragment())
                .commit()
        }
    }
}