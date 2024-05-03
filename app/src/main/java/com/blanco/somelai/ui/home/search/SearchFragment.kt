package com.blanco.somelai.ui.home.search



import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blanco.somelai.R
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

        setClicks()
    }

    private fun setClicks() {

        binding.mcvWineRed.setOnClickListener {
            viewModel.getWineForType("reds")
            navigateToWineListFragment()
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

        binding.mcvCountrySpain.setOnClickListener {
            viewModel.getWinesAndFilterByCountry("Spain")
            navigateToWineListFragment()
        }
        binding.mcvCountryFrance.setOnClickListener {
            viewModel.getWinesAndFilterByCountry("France")
            navigateToWineListFragment()
        }
        binding.mcvCountryPortugal.setOnClickListener {
            viewModel.getWinesAndFilterByCountry("Portugal")
            navigateToWineListFragment()
        }
        binding.mcvCountryItaly.setOnClickListener {
            viewModel.getWinesAndFilterByCountry("Italy")
            navigateToWineListFragment()
        }

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


