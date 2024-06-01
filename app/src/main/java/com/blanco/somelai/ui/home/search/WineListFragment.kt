package com.blanco.somelai.ui.home.search

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
import com.blanco.somelai.databinding.FragmentWineListBinding
import com.blanco.somelai.ui.adapter.WineListAdapter
import com.google.android.material.progressindicator.CircularProgressIndicator

class WineListFragment : Fragment() {

    private lateinit var _binding: FragmentWineListBinding
    private val binding: FragmentWineListBinding get() = _binding

    private val viewModel: WineViewModel by activityViewModels()
    private lateinit var adapter: WineListAdapter

    private lateinit var progressIndicator: CircularProgressIndicator


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWineListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WineListAdapter(
            goToDetail = { wine ->
                val bundle = Bundle().apply {
                    putSerializable("wine", wine)
                }
                findNavController().navigate(R.id.action_wineListFragment_to_wineResponseDetailFragment,bundle)
            }
        )

        binding.rveWineListType.layoutManager = LinearLayoutManager(requireContext())
        binding.rveWineListType.adapter = adapter

        progressIndicator = view.findViewById(R.id.progress_circular)

        observeViewModel()
    }



    private fun showLoadingSpinner() {
        progressIndicator.visibility = View.VISIBLE
    }

    private fun hideLoadingSpinner() {
        progressIndicator.visibility = View.GONE
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner, Observer { uiState ->
            if (uiState.isLoading) {
                showLoadingSpinner()
            } else if (uiState.isError) {
                Log.e("WineListFragment", "isError")
                showErrorMessage()
                hideLoadingSpinner()
            } else {
                // Verificar si la respuesta no es nula y luego actualizar el adaptador
                uiState.response?.let { wines ->
                    adapter.submitList(wines)
                }
                hideLoadingSpinner()
            }
        })
    }

    private fun showErrorMessage() {
        val message = getString(R.string.error_wine_message)
        Log.e("WineListFragment", message)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
