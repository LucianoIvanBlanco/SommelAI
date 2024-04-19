package com.blanco.somelai.data.network.model.body

import com.google.gson.annotations.SerializedName

data class CredentialsBody(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)