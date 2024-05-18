package com.blanco.somelai.ui.home.search



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.blanco.somelai.R
import com.blanco.somelai.databinding.FragmentSearchBinding


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

    // TODO usamos las funciones de tipo especifico del service?
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

        binding.fabCamera.setOnClickListener {
            navigateToScanerCameraFragment()
        }
    }

    private fun navigateToScanerCameraFragment() {
        findNavController().navigate(R.id.action_searchFragment_to_scanerCameraFragment)
    }

    private fun navigateToWineListFragment() {
        findNavController().navigate(R.id.action_searchFragment_to_wineListFragment)
    }

}


