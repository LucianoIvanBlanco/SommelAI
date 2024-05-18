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
import androidx.recyclerview.widget.LinearLayoutManager
import com.blanco.somelai.databinding.FragmentFeedBinding
import com.blanco.somelai.ui.adapter.WineFeedAdapter
import com.blanco.somelai.ui.home.search.WineViewModel
import okhttp3.ResponseBody

class FeedFragment : Fragment() {

    private lateinit var _binding: FragmentFeedBinding
    private val binding get() = _binding

    private lateinit var adapter: WineFeedAdapter
    private val wineViewModel: WineViewModel by activityViewModels()

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
            deleteWine = { wine ->
                wineViewModel.deleteWine(wine)
                Toast.makeText(context, "Vino eliminado", Toast.LENGTH_LONG).show()
            }
        )
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter

        observeWineList()
    }

    private fun observeWineList() {
        wineViewModel.fetchSavedWines()
        wineViewModel.feedUiState.observe(viewLifecycleOwner, Observer { feedUiState ->
            if (feedUiState.isLoading) {
                // TODO agregar spinner de carga aqui
            } else if (feedUiState.isError) {
                showErrorMessage(errorBody = null)
            } else {
                feedUiState.response?.let { wines ->
                    adapter.submitList(wines)
                }
            }
        })
    }

    private fun showErrorMessage(errorBody: ResponseBody?) {
        val message = "Ha habido un error al recuperar los anuncios"
        Log.e("FeedFragment", errorBody.toString())
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


}
