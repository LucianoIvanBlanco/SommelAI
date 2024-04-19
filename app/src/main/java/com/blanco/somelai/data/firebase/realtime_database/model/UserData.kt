package com.blanco.somelai.data.firebase.realtime_database.model

data class UserData(
    // La key será necesaria para guardar más tarde la que reciba
    // de la base de datos. De momento será nula
    var key: String? = null,
    val userId: String = "",
    val userName: String = "",
    val userFullName: String = "",
    val userPassword: String = "",
    val userEmail: String = "",
    val userPhotoUrl: String? = "",
    val wineFavouritesList: List<String>? = emptyList(),

){

}


