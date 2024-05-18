package com.blanco.somelai.data.firebase.authentification

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

// Usado para login y creacion de usuarios en firebase
class EmailAndPasswordAuthenticationManager {

    private val auth = Firebase.auth


// TODO usaremos la funcion?
    fun isUserLogged(): Boolean {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("FirebaseEmailAndPassAuth", "usuario logeado")
        }
        return currentUser != null
    }

    suspend fun createUserFirebaseEmailAndPassword(email: String, password: String): Boolean {
        val result = auth.createUserWithEmailAndPassword(email, password)
        //Esperamos el resultado del registro
        result.await()
        if (result.isSuccessful) {
            Log.d("FirebaseAuth", "createUserFirebaseEmailAndPassword:success")
            return true
        } else {
            Log.e("FirebaseAuth", "createUserFirebaseEmailAndPassword:failure", result.exception)
            return false
        }
    }

    suspend fun signInFirebaseEmailAndPassword(email: String, password: String): Boolean {
        try {
            val result = auth.signInWithEmailAndPassword(email, password)
            //Esperamos el resulta del login
            result.await()
            if (result.isSuccessful) {
                Log.d("FirebaseAuth", "signInFirebaseEmailAndPassword:success")
                return true
            } else {
                Log.d("FirebaseAuth", "signInFirebaseEmailAndPassword:failure", result.exception)
                return false
            }
        } catch (e: FirebaseException) {
            Log.e("FirebaseAuth", "signInFirebaseEmailAndPassword:failure", e)
            return false
        }
    }

    fun signOut() {
        auth.signOut()
    }
}

