package com.blanco.somelai.data.network.model.responses

import com.google.gson.annotations.SerializedName

data class SaveUserResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("token")
    val token: String
)
