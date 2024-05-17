package com.blanco.somelai.ui.home.search

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.blanco.somelai.data.network.WineApi
import com.blanco.somelai.data.network.model.responses.Wine
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class WineViewModel : ViewModel() {

    private var _apiKey: String = "AIzaSyCzEqyGwZ1a8Cz33HPm3_R8dvzMW4c-9k4"

    private val _uiState: LiveData<WineUiState> = MutableLiveData(WineUiState())
    val uiState: LiveData<WineUiState> get() = _uiState

    private val _navigateToWineList = MutableLiveData<Boolean>()
    val navigateToWineList: LiveData<Boolean> get() = _navigateToWineList

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

    private suspend fun getAllWines(): List<Wine> {
        return withContext(Dispatchers.IO) {
            val typesOfWine = listOf("reds", "whites", "rose", "sparkling")
            val wineResponses = mutableListOf<Wine>()

            typesOfWine.forEach { type ->
                try {
                    val response = WineApi.service.getWines(type)
                    if (response.isSuccessful) {
                        response.body()?.let { wineResponses.addAll(it) }
                    }
                } catch (e: Exception) {
                    Log.e("WineViewModel", "Error fetching wines for type $type: ${e.message}")
                }
            }
            wineResponses  // Retornar la lista de vinos recopilada
        }
    }

    fun getWinesAndFilterByCountry(country: String) {
        (uiState as MutableLiveData).value = WineUiState(isLoading = true)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Obtenemos la lista de vinos desde la API de forma asincrónica
                val allWines = getAllWines()

                // Filtrar los vinos por país
                val filteredWines =
                    allWines.filter { it.location.contains(country, ignoreCase = true) }

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

    fun getWinesAndFilterByName(imageUri: Uri, context: Context) {
        (uiState as MutableLiveData).value = WineUiState(isLoading = true)

        val textLiveData = analyzeWineLabel(imageUri, context)
        textLiveData.observeForever { extractedText ->
            viewModelScope.launch {
                try {
                    val wineDetails = extractWineDetails(extractedText)
                    val wineName = wineDetails["name"] ?: ""
                    Log.d("WineViewModel", "Extracted wine name: $wineName")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "$wineName", Toast.LENGTH_LONG).show()
                    }
                    val allWines = getAllWines()
                    val filteredWines = allWines.filter { wine ->
                        wine.wine.lowercase().contains(wineName.lowercase(), ignoreCase = true)
                    }

                    viewModelScope.launch(Dispatchers.Main) {
                        if (filteredWines.isNotEmpty()) {
                            filteredWines.forEach { wine ->
                                Log.d("WineViewModel", "Found wine: ${wine.wine}")
                            }
                            (uiState as MutableLiveData).value = WineUiState(response = filteredWines)
                            _navigateToWineList.value = true
                        } else {
                            (uiState as MutableLiveData).value = WineUiState(isError = true)
                            Toast.makeText(context, "Vino no encontrado en la BBDD", Toast.LENGTH_LONG).show()
                            //Todo si no se encuentran los vinos hay que ir desde el scanerCamera al feedfragment llevando el vino escaneado y afregandolo a una
                            // lista que se guardara en el perfil del usuario
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WineViewModel", "Error fetching wines: ${e.message}")
                    (uiState as MutableLiveData).value = WineUiState(isError = true)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al buscar vinos", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    fun analyzeWineLabel(imageUri: Uri, context: Context): LiveData<String> =
        liveData(Dispatchers.IO) {
            try {
                // Convert Uri to Bitmap
                val imageBitmap: Bitmap =
                    context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    } ?: throw IOException("Failed to read image bytes")

                // Initialize the GenerativeModel
                val model = GenerativeModel(
                    modelName = "gemini-pro-vision",
                    apiKey = _apiKey,
                    generationConfig = generationConfig {
                        temperature = 1f
                        topK = 64
                        topP = 0.95f
                        maxOutputTokens = 8192
                    },
                    safetySettings = listOf(
                        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                        SafetySetting(
                            HarmCategory.SEXUALLY_EXPLICIT,
                            BlockThreshold.MEDIUM_AND_ABOVE
                        ),
                        SafetySetting(
                            HarmCategory.DANGEROUS_CONTENT,
                            BlockThreshold.MEDIUM_AND_ABOVE
                        )
                    )
                )

                // Prepare the prompt and content
                val prompt = """
    Extrae los siguientes datos de la etiqueta del vino:
    1. Nombre del vino
    2. Año del vino
    3. Bodega que lo produce
    4. País/Región/Provincia
    5. Recomendación de maridaje (tipo de comida adecuado al tipo de vino)
    
    Devuelve toda la información como una lista de valores separados por comas, en el orden solicitado. 
    No incluyas los nombres de los campos ni corchetes, solo los datos.
    Utiliza solo 5 comas (,) en total. Si hay más información del mismo campo, usa guiones (-) para separar los subcampos.
    Asegúrate de que la respuesta siga exactamente este formato:
    "Nombre del vino, Año del vino, Bodega que lo produce, País/Región/Provincia, Recomendación de maridaje"
""".trimIndent()


                val inputContent = content {
                    text(prompt)
                    image(imageBitmap)
                }

                // Generate content
                val response = model.generateContent(inputContent)

                // Process the response
                val extractedText = response.candidates.first().content.parts.first().asTextOrNull()
                Log.d("WineViewModel", "Extracted text: $extractedText")

                emit(extractedText ?: "")
            } catch (e: Exception) {
                Log.e(
                    "WineViewModel",
                    "Error analyzing wine label with Gemini: ${e.localizedMessage}",
                    e
                )
                emit("")
            }
        }

    private fun extractWineDetails(extractedText: String): Map<String, String> {
        val details = extractedText
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("'").trim() }

        return mapOf(
            "name" to details.getOrNull(0).orEmpty(),
            "year" to details.getOrNull(1).orEmpty(),
            "winery" to details.getOrNull(2).orEmpty(),
            "country" to details.getOrNull(3).orEmpty(),
            "pairing" to details.getOrNull(4).orEmpty()
        )
    }

    fun resetNavigateToWineList() {
        _navigateToWineList.value = false
    }

}






