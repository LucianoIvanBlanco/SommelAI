package com.blanco.somelai.ui.home.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blanco.somelai.R
import com.blanco.somelai.data.network.WineApi
import com.blanco.somelai.data.network.model.responses.Wine
import com.blanco.somelai.databinding.FragmentFeedBinding
import com.blanco.somelai.databinding.FragmentWineListBinding
import com.blanco.somelai.ui.adapter.WineFeedAdapter
import com.blanco.somelai.ui.adapter.WineListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class WineListFragment : Fragment() {

    private lateinit var _binding: FragmentWineListBinding
    private val binding: FragmentWineListBinding get() = _binding

    private val viewModel: WineViewModel by viewModels()
    val adapter = WineListAdapter()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWineListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // viewModel = ViewModelProvider(this).get(WineViewModel::class.java)
        // adapter = WineListAdapter()
        // Observa los cambios en los datos del ViewModel
//        viewModel.wines.observe(viewLifecycleOwner, Observer { wines ->
//            updateAdapter(wines)
//        })
        binding.rveWineListType.layoutManager = LinearLayoutManager(requireContext())
        binding.rveWineListType.adapter = adapter

        observeViewModel()
    }

    //TODO LOS DATOS LLEGAN CORRECATMENT A _UISTATE EN EL VIEWMODEL. EL PROBLEMSA ES COMO SE ESCUCHA
    // EL CAMBIO AQUI
    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner, Observer { uiState ->
            if (uiState.isLoading) {
                Log.e("WineListFragmetnt", "isLoading")
            } else if (uiState.isError) {
                Log.e("WineListFragmetnt", "isError")
                showErrorMessage()
            } else {
                // Verificar si la respuesta no es nula y luego actualizar el adaptador
                uiState.response?.let { wines ->
                    adapter.submitList(wines)
                    Log.e("WineListFragmetnt", "Enviando pidiendo datos al adapte")
                }
            }
        })
    }

    private fun showErrorMessage() {
        var message = "Ha habido un error al recuperar los vinos"
        Log.e("WineListFragment", message)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}



//    private fun observeViewModel() {
//
//        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
//            if (uiState.response != null) {
//                adapter.submitList(uiState.response)
//                binding.rveWineListType.adapter = adapter
//            } else {
//                showErrorMessage()
//            }
//        }
//
//    }



//    private fun updateAdapter(wines: List<Wine>?) {
//        if (wines.isNullOrEmpty()) {
//            showErrorMessage()
//        } else {
//            viewModel.uiState.observe(viewLifecycleOwner) {uiState ->
//                if (uiState.response != null){
//                    adapter.submitList(uiState.response)
//                }else {
//                    Log.e("ERROR", "Los datos no se actualizan en UpdateAdapter")
//                }
//            }
//        }
//    }





//    private fun getFeedWineList() {
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                val response = WineApi.service.getAllRedsWine()
//                //Comprobamos si la respuesta fue exitosa
//                withContext(Dispatchers.Main) {
//                    if (response.isSuccessful) {
//                        showAds(response.body())
//                    } else {
//                        showErrorMessage(response.errorBody())
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    showErrorMessage(null)
//                }
//            }
//        }
//    }
        //endregion ---- Retrofit ---


        //region ---- Ui related ---
//    private fun showFeed(body: List<Wine>?) {
//        if (body.isNullOrEmpty() == false) {
//            adapter.submitList(body)
//            binding.tvEmptyList.visibility = View.GONE
//        } else {
//            binding.tvEmptyList.visibility = View.VISIBLE
//        }
//    }


        //endregion ---- Ui related ---


        //region ---- Navigation ---
        // Esta no la usaremos porque no tendra detalle, solo tendra boton de borrar

//    private fun goToDetail(advertisement: Advertisement) {
//        val intent = Intent(requireContext(), AdvertisementDetailActivity::class.java)
//        intent.putExtra("advertisementId", advertisement.id)
//        startActivity(intent)
//    }
        //endregion ---- Navigation ---



// TODO Esta funcion borrara nuestra idea de nuestro historial de busqueda en Firebase
//    private fun deleteWine(wine: Wine) {
//        val application = requireActivity().application as MyApplication
//        lifecycleScope.launch(Dispatchers.IO) {
//            application.dataBase.ideaDao().deleteWine(wine)
//        }
//    }

//private fun observeWineList() {
//    val context: Context = this
//    lifecycleScope.launch(Dispatchers.IO) {
//        context.dataStore.data.map { editor ->
//            val wine = editor[stringPreferencesKey("wine")]
//            val winery = editor[stringPreferencesKey("winery")]
//            val location = editor[stringPreferencesKey("location")]
//            val rating = editor[stringPreferencesKey("rating")]
//            val image = editor[stringPreferencesKey("image")]
//            val id = editor[intPreferencesKey("id")]
//
//        }.collect { response ->
//
//            // Si la respuesta no es nula, mostramos el contenido
//            }
//        }
//    }
//}

