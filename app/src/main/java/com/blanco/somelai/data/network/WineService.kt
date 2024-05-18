package com.blanco.somelai.data.network

import com.blanco.somelai.data.network.model.responses.Wine
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface WineService {

    @GET("{type}")
    suspend fun getWines(@Path("type") type: String): Response<List<Wine>>

    @GET("reds")
    suspend fun getAllRedsWine(): Response<List<Wine>>

    @GET("whites")
    suspend fun getAllWhitesWine(): Response<List<Wine>>

    @GET("sparkling")
    suspend fun getAllSparklingWine(): Response<List<Wine>>

    @GET("rose")
    suspend fun getAllRoseWine(): Response<List<Wine>>

}
