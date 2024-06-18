package com.blanco.somelai.ui.home.sommel

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.blanco.somelai.databinding.FragmentSommelBinding
import com.blanco.somelai.ui.home.search.WineViewModel
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

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

        binding.tvResponse.movementMethod = ScrollingMovementMethod()

        setClicks()
        observeResponse()
    }

    private fun setClicks() {
        binding.imageButton.setOnClickListener {
            sendPrompt()
            binding.etAsk.text?.clear()
        }
    }

    private fun sendPrompt() {
        val prompt = binding.etAsk.text.toString().trim()
        if (prompt.isNotEmpty()) {
            lifecycleScope.launch {
                wineViewModel.sendAsk(prompt)
            }
            showProgressIndicator(true)
        }
    }

    private fun observeResponse() {
        wineViewModel.response.observe(viewLifecycleOwner) { response ->
            binding.tvResponse.text = response
            showProgressIndicator(false)
        }
    }

    private fun showProgressIndicator(show: Boolean) {
        binding.progressCircular.visibility = if (show) View.VISIBLE else View.GONE
    }
}
