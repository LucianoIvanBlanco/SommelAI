package com.blanco.somelai.ui.home.search



import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.blanco.somelai.R
import com.blanco.somelai.data.network.WineApi
import com.blanco.somelai.data.network.model.responses.Wine
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.BatchAnnotateImagesRequest
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.cloud.vision.v1.ImageAnnotatorSettings
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class WineViewModel : ViewModel() {

    private var _apiKey: String ="AIzaSyCKRWn_d8zxdLFDgd1WfI4BouqzozuhfFY"

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

//    // FIltramos el texto a comparar
//    private fun preprocessText(text: String): String {
//        // Eliminar puntuación y convertir a minúsculas para una comparación uniforme
//        val cleanedText = text.replace("[^\\w\\s]".toRegex(), "").lowercase()
//
//        // Lista de palabras comunes que pueden ser eliminadas (stop words)
//        val stopWords = setOf("de", "la", "el", "y", "en", "con", "los", "del", "un", "una")
//        return cleanedText.split("\\s+".toRegex())
//            .filter { it.isNotEmpty() && it !in stopWords }
//            .joinToString(" ")
//    }

    fun getWinesAndFilterByName(imageUri: Uri, context: Context) {


        val textLiveData = extractTextFromImageUri(imageUri, context)
        textLiveData.observeForever { extractedText ->
            viewModelScope.launch {
                try {
                    val dataString = textLiveData.toString()
                    Log.d("WineViewModel", "Extracted text: $dataString")
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


    // TODO EXTRACIION DE TEXTO OK. SALE POR CONSOLA TODAAAA LA ETIQUETE


    fun extractTextFromImageUri(imageUri: Uri, context: Context): LiveData<String> = liveData(Dispatchers.IO) {
        Log.d("WineViewModel", "Starting text extraction process")
        try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                val imageBytes = ByteString.readFrom(inputStream)
                Log.d("WineViewModel", "Image bytes read successfully")

                val image = Image.newBuilder().setContent(imageBytes).build()
                val feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build()
                val request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image)
                    .build()

                val requests = listOf(request)
                val batchAnnotateImagesRequest = BatchAnnotateImagesRequest.newBuilder()
                    .addAllRequests(requests)
                    .build()

                // Cargar las credenciales desde el archivo JSON en res/raw
                val credentialsStream: InputStream = context.resources.openRawResource(R.raw.cloudvisioncredentials)
                val credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))

                val settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build()

                ImageAnnotatorClient.create(settings).use { client ->
                    Log.d("WineViewModel", "Sending request to Google Cloud Vision API")
                    val response = client.batchAnnotateImages(batchAnnotateImagesRequest)
                    val textAnnotations = response.responsesList[0].textAnnotationsList
                    if (textAnnotations.isNotEmpty()) {
                        val extractedText = textAnnotations[0].description
                        Log.d("WineViewModel", "Text extracted: $extractedText")
                        emit(extractedText)  // Emitir el resultado a la UI
                    } else {
                        Log.d("WineViewModel", "No text found in the image")
                        emit("No text found")
                    }
                }
            } ?: run {
                Log.e("WineViewModel", "Failed to open image stream")
                emit("Failed to open image stream")
            }
        } catch (e: Exception) {
            Log.e("WineViewModel", "Error extracting text: ${e.localizedMessage}", e)
            emit("Error extracting text: ${e.localizedMessage}")
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






