package com.blanco.somelai.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 *  Necesito:
 *  - Añadir permisos de internet
 *  - Servicio
 *  Las respuestas de la api parseadas para el servicio
 */

object WineApi {
    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.sampleapis.com/wines/")
        .client(client)//Intercepta por consola los datos enviados y recibidos de las peticiones
        .addConverterFactory(GsonConverterFactory.create())//Parsea el json recibido a nuestras data class
        .build()

    val service: WineService by lazy {
        retrofit.create(WineService::class.java)
    }
}