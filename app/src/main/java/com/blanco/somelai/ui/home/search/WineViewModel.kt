package com.blanco.somelai.ui.home.search


import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blanco.somelai.data.network.WineApi
import com.blanco.somelai.data.network.model.responses.Wine
import com.blanco.somelai.ui.adapter.WineListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WineViewModel : ViewModel() {

//    private var _wines: MutableLiveData<List<Wine>?> = MutableLiveData(null)
//    val wines: LiveData<List<Wine>?> get() = _wines

    private var _uiState: MutableLiveData<WineUiState> = MutableLiveData(WineUiState())
    val uiState: LiveData<WineUiState> get() = _uiState


//    private val _loading = MutableLiveData<Boolean>()
//    val loading: LiveData<Boolean> get() = _loading

//    private val _error = MutableLiveData<String>()
//    val error: LiveData<String> get() = _error

//    private val _adapter = MutableLiveData<WineListAdapter>(WineListAdapter())
//    val adapter: LiveData<WineListAdapter> get() = _adapter

    fun getWineForType(typeWine: String) {
        _uiState.postValue(WineUiState(isLoading = true))

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = WineApi.service.getWines(typeWine)
                if (response.isSuccessful) {
                    val wineResponse = response.body()
                    _uiState.postValue(WineUiState(response = response.body()))
                    //sendWines(response.body()) // Llamar a sendWines con la lista de vinos
                    Log.e(
                        "ERROR VIEWMODEL",
                        "RESPONSE recibido en el vireModel y enviado al adapter"
                    )
                    wineResponse?.let {
                        Log.d("WINE_RESPONSE", it.toString())
                    }
                } else {
                    _uiState.postValue(WineUiState(isError = true))
                    Log.e("ERROR VIEWMODEL", "ERROR EN EL RESPONSE")
                }
            } catch (e: Exception) {
                _uiState.postValue(WineUiState(isError = true))
                Log.e("ERROR VIEWMODEL", "Error al cargar los vinos")
            }
        }
    }
}

//    private fun sendWines(body: List<Wine>?) {
//        //Log.d( "REPONSE", body.toString())
//        if (!body.isNullOrEmpty()) { // Simplificación de la condición
//            _adapter.value?.submitList(body)
//        } else {
//            Log.e("ERROR", "ERROR AL ENVIAR DIRECTO AL ADAPTER")
//        }
//    }



//    fun getWineForType(typeWine: String) {
//        _uiState.postValue(WineUiState(isLoading = true))
//
//        viewModelScope.launch(Dispatchers.IO) {
//            val response = WineApi.service.getWines(typeWine)
//            if (response.isSuccessful) {
//                _uiState.postValue(WineUiState(response = response.body()))
//            } else {
//                _uiState.postValue(WineUiState(isError = true))
//            }
//        }
//    }
//
//    init {
//        fetchWines()
//    }
//
//    private fun fetchWines() {
//        viewModelScope.launch {
//            val wines = fetchRedWines()
//            _wines.value = wines
//        }
//    }
//
//    private suspend fun fetchRedWines(): List<Wine> {
//        return withContext(Dispatchers.IO) {
//            val response = WineApi.service.getAllRedsWine()
//            if (response.isSuccessful && response.body()!= null) {
//                response.body()!!
//            } else {
//                emptyList()
//            }
//        }
//    }

