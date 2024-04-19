package com.blanco.somelai.data.network.model.responses

import com.google.gson.annotations.SerializedName

data class UserDataResponse(
    @SerializedName("_id")
    val id: String,
    @SerializedName("userName")
    val userName: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("email")
    val email: String
)