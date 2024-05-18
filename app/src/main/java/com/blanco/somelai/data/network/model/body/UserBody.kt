package com.blanco.somelai.data.network.model.body

import com.google.gson.annotations.SerializedName


// No se usa
data class UserBody(
    @SerializedName("userName")
    val userName: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)
