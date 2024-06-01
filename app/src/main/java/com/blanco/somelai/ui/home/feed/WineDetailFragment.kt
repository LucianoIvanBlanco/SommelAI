package com.blanco.somelai.ui.home.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.blanco.somelai.R
import com.blanco.somelai.data.firebase.realtime_database.RealTimeDatabaseManager
import com.blanco.somelai.data.network.model.body.WineBody
import com.blanco.somelai.databinding.FragmentWineDetailBinding
import com.blanco.somelai.ui.home.search.WineViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class WineDetailFragment : Fragment() {

    private lateinit var _binding: FragmentWineDetailBinding
    private val binding get() = _binding

    private val wineViewModel: WineViewModel by activityViewModels()
    private var currentWine: WineBody? = null
    private val realTimeDatabaseManager = RealTimeDatabaseManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWineDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        extractWineDetails()
        setupListeners()
    }

    private fun extractWineDetails() {
        val wine = arguments?.getSerializable("wine") as? WineBody
        wine?.let {
            currentWine = it
            binding.tvWineTitle.text = it.wine + " " + it.year
            binding.tvWinery.text = it.winery
            binding.tvWineLocation.text = it.country
            binding.tvWineScoreItem.text = it.rating
            binding.tvWinePairing.text = it.pairing
            binding.ratingBar.rating = it.rating.toFloatOrNull() ?: 0f

            Glide.with(this)
                .load(it.image)
                .fitCenter()
                .error(R.drawable.ic_search)
                .into(binding.ivWineImage)
        }
    }

    private fun setupListeners() {
        binding.btnDelete.setOnClickListener {
            currentWine?.let { wine ->
                wineViewModel.deleteWine(wine)
                Toast.makeText(context, getString(R.string.toast_message_delete_wine), Toast.LENGTH_LONG).show()
                requireActivity().onBackPressed()
            }
        }

        binding.btnSave.setOnClickListener {
            currentWine?.let { wine ->
                wine.rating = binding.ratingBar.rating.toString()
                binding.tvWineScoreItem.text = wine.rating
                lifecycleScope.launch {
                    wineViewModel.updateWine(wine)
                    Toast.makeText(context, getString(R.string.toast_message_update_wine), Toast.LENGTH_LONG).show()
                    requireActivity().onBackPressed()
                }
            }
        }
    }
}
