package com.blanco.somelai.ui.home.feed

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.blanco.somelai.R
import com.blanco.somelai.databinding.FragmentFeedBinding
import com.blanco.somelai.ui.adapter.WineFeedAdapter
import com.blanco.somelai.ui.home.search.WineViewModel
import com.google.android.material.progressindicator.CircularProgressIndicator
import okhttp3.ResponseBody

class FeedFragment : Fragment() {

    private lateinit var _binding: FragmentFeedBinding
    private val binding get() = _binding

    private lateinit var adapter: WineFeedAdapter
    private val wineViewModel: WineViewModel by activityViewModels()
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WineFeedAdapter(
            goToDetail = { wine ->
                val bundle = Bundle().apply {
                    putSerializable("wine", wine)
                }
                findNavController().navigate(R.id.action_feedFragment_to_wineDetailFragment, bundle)
            }
        )
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter
        progressIndicator = binding.progressCircular

        observeWineList()
    }

    private fun observeWineList() {
        wineViewModel.fetchSavedWines()
        wineViewModel.feedUiState.observe(viewLifecycleOwner, Observer { feedUiState ->
            if (feedUiState.isLoading) {
                showLoadingSpinner()
            } else if (feedUiState.isError) {
                hideLoadingSpinner()
                showErrorMessage(errorBody = null)
            } else {
                hideLoadingSpinner()
                feedUiState.response?.let { wines ->
                    adapter.submitList(wines)
                }
            }
        })
    }

    private fun showLoadingSpinner() {
        progressIndicator.visibility = View.VISIBLE
    }

    private fun hideLoadingSpinner() {
        progressIndicator.visibility = View.GONE
    }

    private fun showErrorMessage(errorBody: ResponseBody?) {
        val message = "Ha habido un error al recuperar los anuncios"
        Log.e("FeedFragment", errorBody.toString())
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
