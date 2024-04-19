package com.blanco.somelai.ui.home.feed

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.blanco.somelai.databinding.FragmentFeedBinding
import com.blanco.somelai.ui.adapter.WineFeedAdapter
import okhttp3.ResponseBody


class FeedFragment : Fragment() {

    private lateinit var _binding: FragmentFeedBinding
    private val binding get() = _binding

    private lateinit var adapter: WineFeedAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WineFeedAdapter (
            deleteWine = { wine ->
//                deleteWine(wine)
            }
        )
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter

//        observeWineList()

    }


//    override fun onCrete (savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        _binding = FragmentFeedBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        lyfecycleScope.launch(Dispatchers.IO){
//            readDataStorage().collect { response ->
//
//                if (response != null) {
//                    withContext(Dispatchers.Main) {
//                        binding.
//                    }
//
//            }
//        }
//
//
//    }

    //region ---- Retrofit ---

    // TODO funcion para consultas historial de vinos escaneados por el usuario (FEEDFRAGMENT)

//    private fun getFeedWineList() {
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                val response = WineApi.service.getAllAdvertisements()
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

    private fun showErrorMessage(errorBody: ResponseBody?) {
        var message = "Ha habido un error al recuperar los anuncios"
        Log.e("WineList", errorBody.toString())
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    //endregion ---- Ui related ---


    //region ---- Navigation ---
    // Esta no la usaremos porque no tendra detalle, solo tendra boton de borrar

//    private fun goToDetail(advertisement: Advertisement) {
//        val intent = Intent(requireContext(), AdvertisementDetailActivity::class.java)
//        intent.putExtra("advertisementId", advertisement.id)
//        startActivity(intent)
//    }
    //endregion ---- Navigation ---
}


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




