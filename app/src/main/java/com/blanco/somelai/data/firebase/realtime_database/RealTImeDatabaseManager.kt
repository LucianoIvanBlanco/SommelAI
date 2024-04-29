package com.blanco.somelai.data.firebase.realtime_database

import android.util.Log
import com.blanco.somelai.data.firebase.realtime_database.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RealTimeDatabaseManager {

    private val databaseReference = FirebaseDatabase.getInstance().reference

    // No la vamos a usar ya que solo almacenaremos el id de cada vino en la lista (historial)
    //Nos conectamos la nodo de "faves" mediante ".child("faves")". Si quisieramos almacenar
    // más objetos en otras funciones, deberíamos conectarnos a otro child para tener la
    // información separada. Si el nodo no está creado en la base de datos, lo crea al conectarnos.
     suspend fun addUser(user: UserData): UserData? {
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

    suspend fun deleteUser(userId: String) {
        val connection = databaseReference.child("users")
        connection.child(userId).removeValue()
        Log.i("Usuario", "Borrado")
    }


    fun updateUser(user: UserData) {
        //Nos conectamos la nodo de "users" mediante ".child("users")
        val connection = databaseReference.child("users")
        connection.child(user.uid!!).setValue(user)
    }

    suspend fun readUser(userId: String): UserData? {
        val connection = databaseReference.child("users")

        val snapshot = connection.get()
        snapshot.await()

        snapshot.result.children.mapNotNull { dataSnapshot ->
            val user = dataSnapshot.getValue(UserData::class.java)
            Log.i("User", "$user")
            if (user != null && user.uid == userId) {
                return user
            }
        }
        return null
    }

}





