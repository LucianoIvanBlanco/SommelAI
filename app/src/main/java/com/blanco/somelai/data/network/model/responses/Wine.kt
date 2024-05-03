package com.blanco.somelai.data.network.model.responses


import com.google.gson.annotations.SerializedName

data class Wine(
    @SerializedName("winery")
    val winery: String,
    @SerializedName("wine")
    val wine: String,
    @SerializedName("rating")
    val rating: Rating,
    @SerializedName("location")
    val location: String,
    @SerializedName("image")
    val image: String,
    @SerializedName("id")
    val id: Int
)