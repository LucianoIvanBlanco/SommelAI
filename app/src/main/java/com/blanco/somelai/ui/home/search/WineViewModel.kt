package com.blanco.somelai.ui.home.search


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blanco.somelai.data.network.WineApi
import com.blanco.somelai.data.network.model.responses.Wine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

class WineViewModel : ViewModel() {

    private val _uiState: LiveData<WineUiState> = MutableLiveData(WineUiState())
    val uiState: LiveData<WineUiState> get() = _uiState

    // Variable para almacenar temporalmente la respuesta de la API
    private var allWines: List<Wine> = emptyList()

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

    fun getWinesAndFilterByCountry(country: String) {
        (uiState as MutableLiveData).value = WineUiState(isLoading = true)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Realizar la llamada a la API para obtener todos los tipos de vino
                val typesOfWine = listOf("reds", "whites", "rose", "sparkling")
                val wineResponses = mutableListOf<Wine>()

                // Iterar sobre los diferentes tipos de vino
                typesOfWine.forEach { type ->
                    val response = WineApi.service.getWines(type)
                    if (response.isSuccessful) {
                        response.body()?.let { wineResponses.addAll(it) }
                    }
                }

                // Almacenar todas las respuestas de vino en la variable temporal
                allWines = wineResponses

                // Filtrar los vinos por país
                val filteredWines = allWines.filter { it.location.contains(country, ignoreCase = true) }

                viewModelScope.launch(Dispatchers.Main) {
                    // Actualizar el estado de la UI con los vinos filtrados
                    (uiState as MutableLiveData).value = WineUiState(response = filteredWines)
                }
            } catch (e: Exception) {
                // Manejar cualquier error
                (uiState as MutableLiveData).value = WineUiState(isError = true)
            }
        }
    }
}




