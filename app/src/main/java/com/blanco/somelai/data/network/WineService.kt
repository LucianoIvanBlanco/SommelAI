package com.blanco.somelai.data.network

import com.blanco.somelai.data.network.model.body.CredentialsBody
import com.blanco.somelai.data.network.model.body.UserBody
import com.blanco.somelai.data.network.model.body.WineBody
import com.blanco.somelai.data.network.model.responses.BasicResponse
import com.blanco.somelai.data.network.model.responses.SaveUserResponse
import com.blanco.somelai.data.network.model.responses.TokenResponse
import com.blanco.somelai.data.network.model.responses.UserDataResponse
import com.blanco.somelai.data.network.model.responses.Wine
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface WineService {
    // TODO Mejorar el uso del service

    //region --- User ---

    @POST("user")
    fun saveUser(@Body user: UserBody): Call<SaveUserResponse>

    @GET("user")
    suspend fun getUserData(@Header("Authorization") token: String): Response<UserDataResponse>

    @DELETE("user")
    suspend fun deleteUser(@Header("Authorization") token: String): Response<UserDataResponse>

    @POST("user/login")
    fun login(@Body credentialsBody: CredentialsBody): Call<TokenResponse>

    @GET("user/is-username-taken/{userName}")
    fun checkIfUserNameIsTaken(@Path("userName") userName: String): Call<BasicResponse>

    // TODO funcion para consultas historial de vinos escaneados por el usuario (FEEDFRAGMENT)


    //endregion --- User ---


    //region --- Wine ---

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


    // Para guardar en Firebase?
    @POST("create-wine")
    suspend fun createWine(
        @Header("Authorization") token: String,
        @Body wineBody: WineBody
    ): Response<BasicResponse>

    //endregion --- Wine ---
}
