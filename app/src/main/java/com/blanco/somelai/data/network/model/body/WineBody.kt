package com.blanco.somelai.data.network.model.body

import com.google.gson.annotations.SerializedName

data class WineBody(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("image")
    val image: String,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("date")
    val date: String
)

