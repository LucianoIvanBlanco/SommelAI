package com.blanco.somelai.data.network

import com.blanco.somelai.data.network.model.WineModel
import retrofit2.Response
import retrofit2.http.GET

interface WineService {
    // TODO Añadir los endpoints aqui

    @GET("estoEsUnEjemplo")  // TODO cambiar por el endpoint
    suspend fun baseCallr(): Response<WineModel>
}
