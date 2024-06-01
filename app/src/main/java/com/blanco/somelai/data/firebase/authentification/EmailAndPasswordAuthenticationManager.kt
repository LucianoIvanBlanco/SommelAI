package com.blanco.somelai.data.firebase.authentification

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


class EmailAndPasswordAuthenticationManager {

    private val auth = Firebase.auth

    suspend fun createUserFirebaseEmailAndPassword(email: String, password: String): Boolean {
        val result = auth.createUserWithEmailAndPassword(email, password)
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

    suspend fun isEmailRegistered(email: String): Boolean {
        return try {
            val signInMethods = auth.fetchSignInMethodsForEmail(email).await()
            signInMethods.signInMethods?.isNotEmpty() == true
        } catch (e: FirebaseException) {
            Log.e("FirebaseAuth", "isEmailRegistered:failure", e)
            false
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun deleteUserAccount(): Boolean {
        return try {
            val currentUser = auth.currentUser
            currentUser?.delete()?.await()
            Log.d("FirebaseAuth", "deleteUserAccount:success")
            true
        } catch (e: FirebaseException) {
            Log.e("FirebaseAuth", "deleteUserAccount:failure", e)
            false
        }
    }
}


