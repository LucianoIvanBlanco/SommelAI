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


    private var _apiKey: String ="AIzaSyCzEqyGwZ1a8Cz33HPm3_R8dvzMW4c-9k4"

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
        val textLiveData = analyzeWineLabel(imageUri, context)
        textLiveData.observeForever { extractedText ->
            viewModelScope.launch {
                try {
                    Log.d("WineViewModel", "Extracted text: $extractedText")
                    val allWines = getAllWines()
                    val filteredWines = allWines.filter { wine ->
                        wine.wine.lowercase().contains(extractedText.lowercase(), ignoreCase = true)
                    }
                    if (filteredWines.isNotEmpty()) {
                        filteredWines.forEach { wine ->
                            Log.d("WineViewModel", "Found wine: ${wine.wine}")
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "VINO ENCONTRADO!!!!!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Vino no encontrado en la BBDD", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WineViewModel", "Error fetching wines: ${e.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al buscar vinos", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // TODO Si no se puede certificado configurar la apikey correctamente
    // TODO Limpiar Build Gradle con importaciones innecesarias
    // TODO Conexion con IA ok. Extraccion de texto Ok. Ver bien la configuracion del ApiKey a ver si se puede pasar certificado
    // Ver de pasar solo nombre para buscar en la bbdd.

    fun analyzeWineLabel(imageUri: Uri, context: Context): LiveData<String> = liveData(Dispatchers.IO) {
        try {
            // Convert Uri to Bitmap
            val imageBitmap: Bitmap = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
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
                    SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
                    SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
                )
            )

            // Prepare the prompt and content
            val prompt = "Extrae los siguientes datos de la etiqueta del vino: Nombre del vino, Año del vino, Bodega que lo produce, Pais/region/provincia. También un campo 'Recomendación de maridaje' donde recomiendes un tipo de comida adecuado al tipo de vino."
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
            Log.e("WineViewModel", "Error analyzing wine label with Gemini: ${e.localizedMessage}", e)
            emit("")
        }
    }
}

//    suspend fun analyzeText(text: String, apiKey: String): String = withContext(Dispatchers.IO) {
//        try {
//            val client = OkHttpClient()
//            val mediaType = "application/json; charset=utf-8".toMediaType()
//            val jsonObject = JSONObject().apply {
//                put("document", JSONObject().apply {
//                    put("type", "PLAIN_TEXT")
//                    put("content", text)
//                })
//                put("encodingType", "UTF8")
//            }
//            val requestBody = jsonObject.toString().toRequestBody(mediaType)
//
//            val request = Request.Builder()
//                .url("https://language.googleapis.com/v1/documents:analyzeEntities?key=$apiKey")
//                .post(requestBody)
//                .build()
//
//            client.newCall(request).execute().use { response ->
//                val responseBody = response.body?.string()
//                Log.d("AnalyzeText", "Response: $responseBody")
//
//                if (!response.isSuccessful) {
//                    Log.e("AnalyzeText", "Failed to fetch data: ${response.message}")
//                    throw IOException("Unexpected code $response")
//                }
//
//                val responseJson = JSONObject(responseBody ?: "")
//                val entities = responseJson.getJSONArray("entities")
//                val names = entities
//                    .let { 0.until(it.length()).map { i -> it.getJSONObject(i) } }
//                    .filter { it.getString("type") == "PERSON" }
//                    .map { it.getString("name") }
//
//                return@withContext names.joinToString(", ")
//            }
//        } catch (e: Exception) {
//            Log.e("AnalyzeText", "Error in analyzeText: ${e.localizedMessage}", e)
//            throw e
//        }
//    }






