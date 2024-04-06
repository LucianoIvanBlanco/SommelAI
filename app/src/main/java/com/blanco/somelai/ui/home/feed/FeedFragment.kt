package com.blanco.somelai.ui.home.feed

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blanco.somelai.R
import com.blanco.somelai.databinding.FragmentFeedBinding
import com.blanco.somelai.databinding.FragmentProfileBinding

class FeedFragment : Fragment() {

    private lateinit var _binding: FragmentFeedBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

}