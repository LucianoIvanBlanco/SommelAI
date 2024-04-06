package com.blanco.somelai.data.network.model

import com.google.gson.annotations.SerializedName

data class WineType(
    @SerializedName("_id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("image")
    val image: String
)