package com.blanco.somelai.data.network.model.body

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Vino que guardamos en FireBase
data class WineBody(
    @SerializedName("wine")
    val wine: String = "",
    @SerializedName("year")
    val year: String = "",
    @SerializedName("winery")
    val winery: String = "",
    @SerializedName("country")
    val country: String = "",
    @SerializedName("pairing")
    val pairing: String = "",
    @SerializedName("image")
    val image: String = "",
    @SerializedName("id")
    val id: String = "",
    @SerializedName("rating")
    var rating: String = ""
) : Serializable
