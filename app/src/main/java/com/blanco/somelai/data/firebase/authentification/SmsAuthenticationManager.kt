package com.blanco.somelai.data.firebase.authentification

import android.app.Activity
import android.util.Log
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class SmsAuthenticationManager {
    private val auth = Firebase.auth.apply {
        firebaseAuthSettings.forceRecaptchaFlowForTesting(true)
    }

    fun sendInitialCodeSms(
        phoneNumber: String,
        activity: Activity,
        callbacks: OnVerificationStateChangedCallbacks
    ) {
        val token = Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance())
        //Configuramos para que el idioma del Sms sea el español
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Boolean {
        val result = auth.signInWithCredential(credential)
        result.addOnCompleteListener { task->
            Log.i("SmsAuthenticationManager", "Fue exitosa: ${task.isSuccessful}, fue cancelada: ${task.isCanceled}")
        }.await()
        return result.isSuccessful
    }

}