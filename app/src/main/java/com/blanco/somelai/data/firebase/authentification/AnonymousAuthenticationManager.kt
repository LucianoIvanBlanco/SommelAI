package com.blanco.somelai.data.firebase.authentification

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AnonymousAuthenticationManager() {

    private val auth = Firebase.auth

    fun isUserLogged(): Boolean {
        val currentUser = auth.currentUser
        //Comprobamos que no haya ningún usuario autentificado, si lo hay es que ya está loggeado
        if (currentUser != null) {
            Log.d("FirebaseAuth", "Usuario logeado")
        }
        return currentUser != null
    }

    suspend fun signInAnonymously(): Boolean {
        //Guardamos lo que nos devuelva la función ".signInAnonymously()" para comprobar su resultado más adelante.
        val result = auth.signInAnonymously()
        //Decimos a la función que no avance hasta que se resulta la tarea de signIn
        result.await()

        // Con ".isSuccessful" podemos comprobar si el Login tuvo éxito
        if (result.isSuccessful) {
            Log.d("FirebaseAuth", "signInAnonymously:success")
            return true
        } else {
            Log.d("FirebaseAuth", "signInAnonymously:failure", result.exception)
            return false
        }
    }

    fun signOut() {
        //Cerramos sesión del usuario
        auth.signOut()
    }
}