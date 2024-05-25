package com.blanco.somelai.ui.home.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.blanco.somelai.R
import com.blanco.somelai.data.firebase.realtime_database.RealTimeDatabaseManager
import com.blanco.somelai.data.network.model.responses.Wine
import com.blanco.somelai.databinding.FragmentWineResponseDetailBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch


// TODO aqui tendremos que pintar la vista del detalel del vino de la API, agregando info

class WineResponseDetailFragment : Fragment() {

    private lateinit var _binding: FragmentWineResponseDetailBinding
    private val binding get() = _binding

    private val wineViewModel: WineViewModel by activityViewModels()
    private var currentWine: Wine? = null
    private val realTimeDatabaseManager = RealTimeDatabaseManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWineResponseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        extractWineDetails()
    }

    private fun extractWineDetails() {

        val wine = arguments?.getSerializable("wine") as? Wine
    wine?.let {
            getMoreDetails(it.wine, it.winery)
            currentWine = it
            binding.tvWineTitle.text = it.winery
            binding.tvWinery.text = it.wine
            binding.tvWineLocation.text = it.location
            binding.tvWineScoreItem.text = it.rating.average


            Glide.with(this)
                .load(it.image)
                .fitCenter()
                .error(R.drawable.ic_search)
                .into(binding.ivWineImage)
        }
    }

    private fun getMoreDetails(wineName: String, winery: String){
        lifecycleScope.launch {
            val details = wineViewModel.getMoreDetails(wineName, winery)
            binding.tvWineDetails.text = details
        }
    }

}