package com.blanco.somelai.data.firebase.cloud_storage

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
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

    // TODO lo podriamos usar para guardar las fotos de los vinos ClouStorage y no en local
    // Hace falta guardarlos en la nube?
    suspend fun getAllImages(): List<String> {
        //Accedemos al nodo de las imágenes de los anuncios
        val photoReference = storageReference.child("profileImage")
        //Creamos una lista vacía donde añadiremos los enlaces de las fotos
        val imageList = mutableListOf<String>()
        //Pedimos el result de la referencia
        val result: ListResult = photoReference.listAll().await()
        //Iteramos la lista de items de la referencida con un forEach
        result.items.forEach { item ->
            //Añadimos a la lista vacía de los enlaces de descarga de cada item
            imageList.add(item.downloadUrl.toString())
        }
        return imageList
    }

    suspend fun getUserProfilePicture(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Obtenemos la URL de la foto de perfil del usuario
            val photoUrl = it.photoUrl
            // Convertimos la URL a String si es necesario
            return photoUrl.toString()
        }
        // Si no hay usuario logueado, retornamos null
        return null
    }

    suspend fun deleteImage(url: String): Boolean {
        //Recuperamos la referencia a partir del enlace
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
        //Creamos la variable que devolveremos
        var wasSuccess = true

        if (reference != null) {
            reference.delete().addOnFailureListener {
                // Si falla, devolvemos error
                wasSuccess = false
            }.await()
        } else {
            // Si no hay referencia, devolvemos error
            wasSuccess = false
        }
        return wasSuccess
    }
}


