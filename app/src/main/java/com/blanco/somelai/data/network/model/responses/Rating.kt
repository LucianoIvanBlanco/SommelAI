package com.blanco.somelai.data.network.model.responses

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Rating(
    @SerializedName("average")
    val average: String,
    @SerializedName("reviews")
    val reviews: String
) : Serializable