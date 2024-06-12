package com.blanco.somelai.ui.home.sommel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.blanco.somelai.databinding.FragmentSommelBinding
import com.blanco.somelai.ui.home.search.WineViewModel
import com.google.android.material.progressindicator.CircularProgressIndicator


class SommelFragment : Fragment() {

    private lateinit var _binding: FragmentSommelBinding
    private val binding get() = _binding

    private val wineViewModel: WineViewModel by activityViewModels()
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSommelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressIndicator = binding.progressCircular

    }

}