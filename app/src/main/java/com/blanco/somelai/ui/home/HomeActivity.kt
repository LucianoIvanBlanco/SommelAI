package com.blanco.somelai.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.blanco.somelai.R
import com.blanco.somelai.databinding.ActivityHomeBinding

// TODO cuando se pasa de un fragment que no esta en el fcv_home (uno de los hijos) a uno que si esta, se queda la seleccion del fragment anterior

class HomeActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityHomeBinding
    private val binding: ActivityHomeBinding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setBottomNavigationView()
    }

    private fun setBottomNavigationView() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fcv_home) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bnvHome.setupWithNavController(navController)
    }
}