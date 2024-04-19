package com.blanco.somelai.ui.home.search


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.blanco.somelai.data.storage.dataStore
import com.blanco.somelai.databinding.FragmentSearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    private lateinit var _binding: FragmentSearchBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root

        binding.fabCamera.setOnClickListener{
            openCamera()
        }
    }

    private fun openCamera() {
        // TODO logica para abrir la camara, tendremos que pedir permiso

        lifecycleScope.launch(Dispatchers.IO) {
            val wine: String
            val winery: String
            val location: String
            val rating: String
            val image: String
            val id: Int

            saveDataInDataStore(wine= "", winery="", location="", rating="", image="", id=0)
        }
    }

    private suspend fun saveDataInDataStore(
        wine: String, winery: String, location: String, rating: String, image: String, id: Int) {

        val context: Context = requireContext()
            context.dataStore.edit { editor ->
                editor[stringPreferencesKey("wine")] = wine
                editor[stringPreferencesKey("winery")] = winery
                editor[stringPreferencesKey("location")] = location
                editor[stringPreferencesKey("rating")] = rating
                editor[stringPreferencesKey("image")] = image
                editor[intPreferencesKey("id")] = id

            }
    }




}

