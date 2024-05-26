package com.blanco.somelai.ui.home.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.blanco.somelai.R
import com.blanco.somelai.databinding.FragmentSearchNameBinding
import com.blanco.somelai.ui.adapter.WineSearchAdapter
import com.blanco.somelai.ui.home.HomeActivity

class SearchNameFragment : Fragment() {

    private lateinit var binding: FragmentSearchNameBinding
    private lateinit var adapter: WineSearchAdapter
    private val viewModel: WineViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WineSearchAdapter { wine ->
            val bundle = Bundle().apply {
                putSerializable("wine", wine)
            }
            findNavController().navigate(R.id.action_searchNameFragment_to_wineResponseDetailFragment, bundle)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        setupSearch()
        observeViewModel()
        focusSearchField()
    }

    private fun setupSearch() {
        binding.etSearchBottom.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                (activity as HomeActivity).showSpinner()
                viewModel.searchWinesByName(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            uiState.response?.let { wines ->
                adapter.setWines(wines)
                (activity as HomeActivity).hideSpinner()
            }
        }
    }

    private fun focusSearchField() {
        binding.etSearchBottom.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearchBottom, InputMethodManager.SHOW_IMPLICIT)
    }
}
