package com.blanco.somelai.ui.home.search


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blanco.somelai.R
import com.blanco.somelai.data.network.model.responses.Wine
import com.blanco.somelai.data.storage.dataStore
import com.blanco.somelai.databinding.FragmentSearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    private lateinit var _binding: FragmentSearchBinding
    private val binding get() = _binding

    private val viewModel: WineViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mcvWineRed.setOnClickListener {
            viewModel.getWineForType("reds")
            navigateToWineListFragment()
            //navigateToWineListFragment()
        }

        setClicks()
    }

    private fun setClicks() {

        binding.mcvWineRed.setOnClickListener {
            navigateToWineListFragment()
            viewModel.getWineForType("reds")
            //navigateToWineListFragment()
        }
        binding.mcvWineWhite.setOnClickListener {
            viewModel.getWineForType("whites")
            navigateToWineListFragment()
        }
        binding.mcvWineRose.setOnClickListener {
            viewModel.getWineForType("rose")
            navigateToWineListFragment()
        }
        binding.mcvWineSparkling.setOnClickListener {
            viewModel.getWineForType("sparkling")
            navigateToWineListFragment()
        }


//        binding.mcvCountrySpain.setOnClickListener {
//            viewModel.getWineForType("Spain")
//        }
//        binding.mcvCountryFrance.setOnClickListener {
//            viewModel.getWineForType("France")
//        }
//        binding.mcvCountryPortugal.setOnClickListener {
//            viewModel.getWineForType("Portugal")
//        }
//        binding.mcvCountryItaly.setOnClickListener {
//            viewModel.getWineForType("Italy")
//        }

        binding.fabCamera.setOnClickListener{
            openCamera()
        }
    }

    private fun navigateToWineListFragment() {
        findNavController().navigate(R.id.action_searchFragment_to_wineListFragment)
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

            // saveDataInDataStore(wine= "", winery="", location="", rating="", image="", id=0)
        }
    }

    private fun showMessage(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

}

//    private fun fetchWineList(type: String) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val url = "https://api.sampleapis.com/wines/$type"
//            val response = HttpClient.newHttpClient().get(url)
//            if (response.isSuccessful) {
//                val wines = response.body()?.parseJsonToListOfWines()?: emptyList()
//                navigateToWineListFragment(wines)
//            } else {
//                showMessage("Error al cargar la lista de vinos.")
//            }
//        }
//    }

//    private fun navigateToWineListFragment(wines: List<Wine>) {
//        val action = WineListFragmentDirections.actionGlobalWineListFragment(wines)
//        findNavController().navigate(action)
//    }



//    private suspend fun saveDataInDataStore(
//        wine: String, winery: String, location: String, rating: String, image: String, id: Int) {
//
//        val context: Context = requireContext()
//            context.dataStore.edit { editor ->
//                editor[stringPreferencesKey("wine")] = wine
//                editor[stringPreferencesKey("winery")] = winery
//                editor[stringPreferencesKey("location")] = location
//                editor[stringPreferencesKey("rating")] = rating
//                editor[stringPreferencesKey("image")] = image
//                editor[intPreferencesKey("id")] = id
//
//            }
//    }


