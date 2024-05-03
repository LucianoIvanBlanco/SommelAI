package com.blanco.somelai.ui.home.search


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blanco.somelai.data.network.WineApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WineViewModel : ViewModel() {

    private val _uiState: LiveData<WineUiState> = MutableLiveData(WineUiState())
    val uiState: LiveData<WineUiState> get() = _uiState

    fun getWineForType(typeWine: String) {
        (uiState as MutableLiveData).value = WineUiState(isLoading = true)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = WineApi.service.getWines(typeWine)
                viewModelScope.launch(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val wineResponse = response.body()
                        (uiState as MutableLiveData).setValue(WineUiState(response = wineResponse))

                    } else {
                        (uiState as MutableLiveData).setValue(WineUiState(isError = true))
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR VIEW MODEL", "Error al cargar los vinos")
            }
        }
    }
}

