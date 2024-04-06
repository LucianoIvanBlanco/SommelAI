package com.blanco.somelai.ui.home.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blanco.somelai.databinding.FragmentSearchBinding
import com.blanco.somelai.ui.adapter.WineCountryAdapter
import com.blanco.somelai.ui.adapter.WineTypeAdapter

class SearchFragment : Fragment() {

    private lateinit var _binding: FragmentSearchBinding
    private val binding get() = _binding

    private lateinit var wineCountryAdapter: WineCountryAdapter
    private lateinit var wineTypeAdapter: WineTypeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

}