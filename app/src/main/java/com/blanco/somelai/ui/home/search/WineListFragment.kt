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

class WineListFragment : Fragment() {

    private lateinit var _binding: FragmentWineListBinding
    private val binding: FragmentWineListBinding get() = _binding

    private val viewModel: WineViewModel by activityViewModels()
    private lateinit var adapter: WineListAdapter

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

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner, Observer { uiState ->
            if (uiState.isLoading) {
                // TODO agregar spinner de carga aqui
            } else if (uiState.isError) {
                Log.e("WineListFragment", "isError")
                showErrorMessage()
            } else {
                // Verificar si la respuesta no es nula y luego actualizar el adaptador
                uiState.response?.let { wines ->
                    adapter.submitList(wines)
                }
            }
        })
    }

    private fun showErrorMessage() {
        val message = "Ha ocurrido un error al recuperar los datos"
        Log.e("WineListFragment", message)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
