package com.blanco.somelai.data.firebase.realtime_database.model

import com.blanco.somelai.data.network.model.body.WineBody

data class UserData(
    // La key será necesaria para guardar más tarde la que reciba
    // de la base de datos. De momento será nula
    var key: String? = null,
    val uid: String = "",
    val userEmail: String = "",
    val userFullName: String = "",
    val userName: String = "",
    val userPassword: String = "",
    val userPhotoUrl: String? = "",
    val wineFavouritesList: MutableList<WineBody> = mutableListOf()

){

}


