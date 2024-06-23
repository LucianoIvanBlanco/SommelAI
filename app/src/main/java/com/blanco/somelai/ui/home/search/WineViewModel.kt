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
import com.blanco.somelai.data.firebase.cloud_storage.CloudStorageManager
import com.blanco.somelai.data.firebase.realtime_database.RealTimeDatabaseManager
import com.blanco.somelai.data.network.WineApi
import com.blanco.somelai.data.network.model.body.WineBody
import com.blanco.somelai.data.network.model.responses.Wine
import com.blanco.somelai.ui.home.feed.WineBodyUiState
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.util.UUID

class WineViewModel : ViewModel() {

    private var _apiKey: String = "AIzaSyCzEqyGwZ1a8Cz33HPm3_R8dvzMW4c-9k4"

    private val _uiState: LiveData<WineUiState> = MutableLiveData(WineUiState())
    val uiState: LiveData<WineUiState> get() = _uiState

    private val _navigateToWineList = MutableLiveData<Boolean>()
    val navigateToWineList: LiveData<Boolean> get() = _navigateToWineList

    private val _navigateToWineFeed = MutableLiveData<Boolean>()
    val navigateToWineFeed: LiveData<Boolean> get() = _navigateToWineFeed

    private val _feedUiState: LiveData<WineBodyUiState> = MutableLiveData(WineBodyUiState())
    val feedUiState: LiveData<WineBodyUiState> get() = _feedUiState

    private val _response = MutableLiveData<String>()
    val response: LiveData<String> get() = _response

    private var realTimeDatabaseManager: RealTimeDatabaseManager = RealTimeDatabaseManager()
    private var cloudStorageManager: CloudStorageManager = CloudStorageManager()

    private var allWinesCache: List<Wine> = emptyList()

    private val prompt: String = """
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

    init {
        // Cargar todos los vinos al iniciar la aplicación
        viewModelScope.launch {
            loadAllWines()
        }
    }
    private suspend fun loadAllWines() {
        try {
            val wines = getAllWines()
            allWinesCache = wines
        } catch (e: Exception) {
            Log.e("WineViewModel", "Error loading all wines: ${e.message}")
        }
    }

    private suspend fun getAllWines(): List<Wine> {
        return withContext(Dispatchers.IO) {
            val wineResponses = mutableListOf<Wine>()
            val deferredResponses = listOf(
                async { fetchWines(WineApi.service::getAllRedsWine) },
                async { fetchWines(WineApi.service::getAllWhitesWine) },
                async { fetchWines(WineApi.service::getAllSparklingWine) },
                async { fetchWines(WineApi.service::getAllRoseWine) }
            )
            deferredResponses.awaitAll().forEach { wines ->
                wineResponses.addAll(wines)
            }
            wineResponses
        }
    }

    fun searchWinesByName(query: String) {
        val filteredWines = allWinesCache.filter { wine ->
            wine.winery.lowercase().contains(query.lowercase(), ignoreCase = true)
        }
        (uiState as MutableLiveData).value = WineUiState(response = filteredWines)
    }

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
                Log.e("WineViewModel", "Error al cargar los vinos")
            }
        }
    }

    private suspend fun fetchWines(fetchFunction: suspend () -> Response<List<Wine>>): List<Wine> {
        return try {
            val response = fetchFunction()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("WineViewModel", "Error fetching wines: ${e.message}")
            emptyList()
        }
    }


    fun getWinesAndFilterByCountry(country: String) {
        (uiState as MutableLiveData).value = WineUiState(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filteredWines =
                    allWinesCache.filter { it.location.contains(country, ignoreCase = true) }
                viewModelScope.launch(Dispatchers.Main) {
                    (uiState as MutableLiveData).value = WineUiState(response = filteredWines)
                }
            } catch (e: Exception) {
                (uiState as MutableLiveData).value = WineUiState(isError = true)
                Log.e("WineViewModel", "Error: ${e.message}")
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
                    val nameAndYear = wineDetails["name"] + wineDetails["year"]
                    val moreDetails = getMoreDetails(nameAndYear, wineDetails["winery"] ?: "")
                    val wineName = wineDetails["name"] ?: ""
                    Log.d("WineViewModel", "Extracted wine name: $wineName")
                    val filteredWines = allWinesCache.filter { wine ->
                        wine.winery.lowercase().contains(wineName.lowercase())
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        if (filteredWines.isNotEmpty()) {
                            filteredWines.forEach { wine ->
                                Log.d("WineViewModel", "Found wine: ${wine.wine}")
                            }
                            Toast.makeText(
                                context,
                                "SE ENCONTRARON COINCIDENCIAS EN LA BUSQUEDA",
                                Toast.LENGTH_LONG
                            ).show()
                            (uiState as MutableLiveData).value =
                                WineUiState(response = filteredWines)
                            _navigateToWineList.value = true
                        } else {
                            Toast.makeText(
                                context,
                                "SIN COINCIDENCIAS EN LA BUSQUEDA, GUARDANDO VINO",
                                Toast.LENGTH_LONG
                            ).show()
                            //Toast.makeText(context, "GUARDANDO VINO", Toast.LENGTH_LONG).show()


                            val imageWine = uploadImage(imageUri).toString()
                            createAndSaveWine(wineDetails, moreDetails, imageWine)

                        }
                    }
                } catch (e: Exception) {
                    Log.e("WineViewModel", "Error al filtrar vinos: ${e.message}")
                    (uiState as MutableLiveData).value = WineUiState(isError = true)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun createAndSaveWine(
        wineDetails: Map<String, String>,
        moreDetails: String,
        imageUri: String
    ) {
        val newWine = WineBody(
            wine = wineDetails["name"] ?: "",
            year = wineDetails["year"] ?: "",
            winery = wineDetails["winery"] ?: "",
            country = wineDetails["country"] ?: "",
            pairing = moreDetails,
            image = imageUri,
            id = UUID.randomUUID().toString(),
            rating = ""
        )
        viewModelScope.launch {
            try {
                realTimeDatabaseManager.saveWine(newWine)

                fetchSavedWines()
                _navigateToWineFeed.value = true
                Log.d("WineViewModel", "Wine saved successfully")
            } catch (e: Exception) {
                Log.e("WineViewModel", "Error saving wine: ${e.message}")
            }
        }
    }


    private suspend fun uploadImage(selectedImageUri: Uri?): String? {
        return withContext(Dispatchers.IO) {
            try {
                cloudStorageManager.uploadWineImage(selectedImageUri!!)
            } catch (e: Exception) {
                Log.e("uploadImage", "Error uploading image: ${e.message}")
                null
            }
        }
    }

    fun fetchSavedWines() {
        viewModelScope.launch {
            try {
                (feedUiState as MutableLiveData).value = WineBodyUiState(isLoading = true )
                val savedWines = realTimeDatabaseManager.getSavedWines()
                (feedUiState as MutableLiveData).value = WineBodyUiState(response = savedWines)
                Log.d("WineViewModel", "Fetched ${savedWines.size} saved wines")
            } catch (e: Exception) {
                Log.e("WineViewModel", "Error fetching saved wines: ${e.message}")
                (feedUiState as MutableLiveData).value = WineBodyUiState(isError = true)
            }
        }
    }

    fun deleteWine(wine: WineBody) {
        viewModelScope.launch {
            try {
                RealTimeDatabaseManager().deleteUserWine(wine)
                fetchSavedWines()
            } catch (e: Exception) {
                Log.e("WineViewModel", "Error deleting wine", e)
            }
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

    fun resetNavigateToFeedFragment() {
        _navigateToWineFeed.value = false
        Log.d("WineViewModel", "navigateToWineFeed")
    }

    fun updateWine(wine: WineBody) {
        viewModelScope.launch {
            try {
                RealTimeDatabaseManager().updateWineRating(wine)
                fetchSavedWines()
            } catch (e: Exception) {
                Log.e("WineViewModel", "Error update wine", e)
            }
        }
    }

    fun analyzeWineLabel(imageUri: Uri, context: Context): LiveData<String> =
        liveData(Dispatchers.IO) {
            try {
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

                val inputContent = content {
                    text(prompt)
                    image(imageBitmap)
                }

                val response = model.generateContent(inputContent)
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


    suspend fun getMoreDetails(wineName: String, winery: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val model = GenerativeModel(
                    modelName = "gemini-1.5-flash-latest",
                    apiKey = _apiKey,
                    generationConfig = generationConfig {
                        temperature = 1f
                        topK = 64
                        topP = 0.95f
                        maxOutputTokens = 1000
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
                val prompt = """
                Proporciona información concisa sobre el vino "$wineName" de la bodega "$winery" en un máximo de 60 palabras. 
                Si no hay coincidencia exacta, proporciona información de un vino similar sin mencionarlo explícitamente.
            """.trimIndent()
                val inputContent = content {
                    text(prompt)
                }

                val response = model.generateContent(inputContent)
                val extractedText = response.candidates.first().content.parts.first().asTextOrNull()
                Log.d("WineViewModel", "Extracted text: $extractedText")
                extractedText ?: ""
            } catch (e: Exception) {
                Log.e(
                    "WineViewModel",
                    "Error analyzing wine label with Gemini: ${e.localizedMessage}",
                    e
                )
                "No hay más informacion disponible sobre este vino."
            }
        }
    }

    suspend fun sendAsk(prompt: String) {
        withContext(Dispatchers.IO) {
            try {
                val model = GenerativeModel(
                    modelName = "gemini-1.5-flash-latest",
                    apiKey = _apiKey,
                    generationConfig = generationConfig {
                        temperature = 1f
                        topK = 64
                        topP = 0.95f
                        maxOutputTokens = 1000
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
                val rules = """
                   Eres un experto en vinos. Limitate a responder en ese contexto la pregunta ingresada de forma puntual. Cada respuesta no debe tener mas de 150 palabras.
                """.trimIndent()
                val inputContent = content {
                    text(rules + prompt)
                }

                val response = model.generateContent(inputContent)
                val extractedText = response.candidates.first().content.parts.first().asTextOrNull()
                Log.d("WineViewModel", "Response: $extractedText")
                _response.postValue(extractedText ?: "")
            } catch (e: Exception) {
                Log.e(
                    "WineViewModel",
                    "Error analyzing wine label with Gemini: ${e.localizedMessage}",
                    e
                )
                _response.postValue("Ha ocurrido un error, por favor intenta de nuevo.")
            }
        }
    }
}