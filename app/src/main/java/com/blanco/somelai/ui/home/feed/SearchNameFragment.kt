package com.blanco.somelai.ui.home.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blanco.somelai.R
import com.blanco.somelai.data.network.model.responses.Wine
import com.blanco.somelai.ui.adapter.WineSearchAdapter
import com.google.android.material.textfield.TextInputEditText

class SearchNameFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WineSearchAdapter
    private lateinit var etSearchBottom: TextInputEditText
    private val viewModel: WineViewModel by activityViewModels()

    init {
        adapter = WineSearchAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_name, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        etSearchBottom = view.findViewById(R.id.et_search_bottom)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        setupSearch()
        observeViewModel()
        focusSearchField()
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        etSearchBottom.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchWinesByName(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            uiState.response?.let { wines ->
                adapter.setWines(wines)
            }
        }
    }

    fun setWines(wines: List<Wine>) {
        adapter.setWines(wines)
    }

    fun focusSearchField() {
        etSearchBottom.requestFocus()
    }
}
