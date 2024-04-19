package com.blanco.somelai.data.network.model.responses

import com.google.gson.annotations.SerializedName

data class BasicResponse(
    @SerializedName("message")
    val message: String
)

