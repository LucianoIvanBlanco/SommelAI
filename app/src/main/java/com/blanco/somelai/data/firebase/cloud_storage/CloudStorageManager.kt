package com.blanco.somelai.data.firebase.cloud_storage

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

class CloudStorageManager {

    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    suspend fun uploadProfileImage(uri: Uri): String? {
        val imageName = "$uri".replace(
            "/",
            "_"
        )
        var imageUrl: String? = null
        val photoReference = storageReference.child("profileImage/$imageName")

        photoReference.putFile(uri).continueWithTask { task ->
            if (!task.isSuccessful) {
                Log.e("CloudStorageManager", "No se ha podido subir la foto")
                task.exception?.let { throw it }
            }
            photoReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageUrl = "${task.result}"
                Log.i("User", "Enlace de la foto: $imageUrl")
            } else {
                Log.e("User", "No se ha podido subir la foto")
            }
        }.await()
        return imageUrl
    }

    suspend fun uploadWineImage(uri: Uri): String? {
        val imageName = "$uri".replace(
            "/",
            "_"
        )
        var imageUrl: String? = null
        val photoReference = storageReference.child("wineImage/$imageName")

        photoReference.putFile(uri).continueWithTask { task ->
            if (!task.isSuccessful) {
                Log.e("CloudStorageManager", "No se ha podido subir la foto del vino")
                task.exception?.let { throw it }
            }
            photoReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageUrl = "${task.result}"
                Log.i("User", "Enlace de la foto del vino: $imageUrl")
            } else {
                Log.e("User", "No se ha podido subir la foto del vino")
            }
        }.await()
        return imageUrl
    }

    suspend fun getUserProfilePicture(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val photoUrl = it.photoUrl
            return photoUrl.toString()
        }
        return null
    }

    suspend fun deleteImage(url: String): Boolean {

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
        var wasSuccess = true
        if (reference != null) {
            reference.delete().addOnFailureListener {
                wasSuccess = false
            }.await()
        } else {
            wasSuccess = false
        }
        return wasSuccess
    }
}


