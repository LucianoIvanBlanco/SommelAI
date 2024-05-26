package com.blanco.somelai.data.firebase.realtime_database

import android.util.Log
import com.blanco.somelai.data.firebase.realtime_database.model.UserData
import com.blanco.somelai.data.network.model.body.WineBody
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RealTimeDatabaseManager {

    private val databaseReference = FirebaseDatabase.getInstance().reference

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

    suspend fun saveWine(wine: WineBody) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val userRef = databaseReference.child("users").child(userId)
            val snapshot = userRef.get().await()
            val userData = snapshot.getValue(UserData::class.java)

            if (userData != null) {
                val updatedWineList = userData.wineFavouritesList.toMutableList()
                updatedWineList.add(wine)
                userRef.child("wineFavouritesList").setValue(updatedWineList).await()
                Log.d("RealTimeDatabaseManager", "Wine saved successfully")
            } else {
                Log.e("RealTimeDatabaseManager", "User data not found")
            }
        } else {
            Log.e("RealTimeDatabaseManager", "User not logged in")
        }
    }

    suspend fun getSavedWines(): List<WineBody> {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val userRef = databaseReference.child("users").child(userId)
            val snapshot = userRef.child("wineFavouritesList").get().await()
            val wineList = snapshot.children.mapNotNull { it.getValue(WineBody::class.java) }
            Log.d("RealTimeDatabaseManager", "Retrieved ${wineList.size} wines")
            return wineList
        } else {
            Log.e("RealTimeDatabaseManager", "User not logged in")
            return emptyList()
        }
    }

    suspend fun deleteUserWine(wine: WineBody) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val userRef = databaseReference.child("users").child(userId)
            val snapshot = userRef.get().await()
            val userData = snapshot.getValue(UserData::class.java)

            if (userData != null) {
                val updatedWineList = userData.wineFavouritesList.toMutableList()
                updatedWineList.remove(wine)
                userRef.child("wineFavouritesList").setValue(updatedWineList).await()
                Log.d("RealTimeDatabaseManager", "Wine deleted successfully")
            } else {
                Log.e("RealTimeDatabaseManager", "User data not found")
            }
        } else {
            Log.e("RealTimeDatabaseManager", "User not logged in")
        }
    }

    suspend fun updateWineRating(wine: WineBody) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val userRef = databaseReference.child("users").child(userId)
            val snapshot = userRef.get().await()
            val userData = snapshot.getValue(UserData::class.java)
            val wineId = wine.id
            val newRating = wine.rating

            if (userData != null) {
                val updatedWineList = userData.wineFavouritesList.map { wine ->
                    if (wine.id == wineId) {
                        wine.copy(rating = newRating)
                    } else {
                        wine
                    }
                }
                userRef.child("wineFavouritesList").setValue(updatedWineList).await()
                Log.d("RealTimeDatabaseManager", "Wine rating updated successfully")
            } else {
                Log.e("RealTimeDatabaseManager", "User data not found")
            }
        } else {
            Log.e("RealTimeDatabaseManager", "User not logged in")
        }
    }
}






