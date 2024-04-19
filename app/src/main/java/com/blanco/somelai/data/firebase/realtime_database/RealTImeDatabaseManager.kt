package com.blanco.somelai.data.firebase.realtime_database

import android.util.Log
import com.blanco.somelai.data.firebase.realtime_database.model.UserData
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RealTimeDatabaseManager {

    private val databaseReference = FirebaseDatabase.getInstance().reference

    // No la vamos a usar ya que solo almacenaremos el id de cada vino en la lista (historial)
     suspend fun addUser(user: UserData): UserData? {
        //Nos conectamos la nodo de "faves" mediante ".child("faves")". Si quisieramos almacenar
        // más objetos en otras funciones, deberíamos conectarnos a otro child para tener la
        // información separada. Si el nodo no está creado en la base de datos, lo crea al conectarnos.
        val connection = databaseReference.child("users")
        //Creamos una key
        val key = connection.push().key
        //Si no es nula, guardamos el usuario
        if (key != null) {
            //Hacemos una copia del usuario asignándole la key
            val userWithKey = user.copy(key=key)
            // Le asignamos un id al usuario que sera la "key"
            connection.child(key).setValue(userWithKey).await()
            //connection.child("${user.userId}").setValue(userWithKey).await()
            //Si todo ha ido bien, retornamos el objeto con su key
            Log.d("usuario", "guardado")
            return userWithKey
        } else {
            Log.e("usuario", "fallo")
            // Si no hemos podido crear la key retornamos null para saber que no se guardó
            return null
        }
    }

    fun deleteUser(userId: String) {
        val connection = databaseReference.child("users")
        connection.child(userId).removeValue()
        Log.i("Usuario", "Borrado")
    }


    fun updateUser(user: UserData) {
        //Nos conectamos la nodo de "users" mediante ".child("users")
        val connection = databaseReference.child("users")
        connection.child(user.key!!).setValue(user)
    }

//    suspend fun readUser(userName: String): UserData? {
//        val connection = databaseReference.child("users")
//
//        val snapshot = connection.get()
//        snapshot.await()
//
//        snapshot.children.mapNotNull { dataSnapshot ->
//            val user = dataSnapshot.getValue(UserData::class.java)
//            Log.i("User", "$user")
//            if (user != null && user.userId == userName) {
//                return user
//            }
//        }
//        return null
//    }


    }


//    fun updateFavourite(favourite: FavouriteAdvertisement) {
//        //Nos conectamos la nodo de "faves" mediante ".child("faves")"
//        val connection = databaseReference.child("faves")
//        //Actualizamos este favorito por su id, si os dais cuenta es la misma función
//        // que para crear un nuevo dato. Solo que en este caso el key ya existía
//        // previamente y sobreescribimos la información
//        connection.child(favourite.advertisementId).setValue(favourite)
//    }

//    suspend fun readFavourite(advertisementId: String): FavouriteAdvertisement? {
//        //Nos conectamos la nodo de "faves" mediante ".child("faves")"
//        val connection = databaseReference.child("faves")
//
//        val snapshot = connection.get()
//        //Esperamos la conexión para recoger la lista
//        snapshot.await()
//
//        snapshot.result.children.mapNotNull { dataSnapshot ->
//            //Recogemos cada favorito y le asignamos su key si no son nulos
//            val favourite = dataSnapshot.getValue(FavouriteAdvertisement::class.java)
//            Log.i("Fave", "$favourite")
//            if (favourite != null && favourite.advertisementId == advertisementId) {
//                //Si encontramos el anuncio que coincida el ID, lo retornamos
//                return favourite
//            }
//        }
//        //Si NO encontramos el anuncio que coincida el ID, devolvemos un nulo
//        return null
//    }


