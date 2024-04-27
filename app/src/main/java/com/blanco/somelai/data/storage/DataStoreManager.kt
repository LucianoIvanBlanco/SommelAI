package com.blanco.somelai.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.blanco.somelai.data.network.model.responses.UserDataResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "SOMELAI_STORE")

class DataStoreManager (val context: Context) {

    private val emailKey = "EMAIL"
    private val passwordKey = "PASSWORD"
    private val jwtKey = "JWT"
    private val userNameKey = "USERNAME"
    private val fullNameKey = "FULLNAME"
    private val userPhotoUrl = "PHTOTO_URL"
    private val userIdKey = "USER_ID"
    private val isLoggedKey = "IS_LOGGED"

    private val wine = "WINE"
    private val winery = "WINERY"
    private val location = "LOCATION"
    private val rating = "RATING"
    private val image = "IMAGE"
    private val id = "ID"

    val sampleKey = stringPreferencesKey("SAMPLE_KEY")

    suspend fun saveData (context: Context, sampleData: String) {
        context.dataStore.edit { editor ->
            editor[sampleKey] = sampleData
        }
    }

    suspend fun getSampleData(context: Context): Flow<String> {
        return context.dataStore.data.map { editor ->
            editor[sampleKey] ?: "No hay datos"
        }
    }

    suspend fun deleteAll(context: Context) {
        context.dataStore.edit { editor ->
            editor.clear() // Borrar todos
        }
    }

    suspend fun deleteSample(context: Context) {
        context.dataStore.edit { editor ->
            editor.remove(sampleKey) // borrar solo un dato
        }

    }

    private suspend fun putString(key: String, value: String) {
        context.dataStore.edit { editor ->
            editor[stringPreferencesKey(key)] = value
        }
    }

    private suspend fun putBoolean(key: String, value: Boolean) {
        context.dataStore.edit { editor ->
            editor[booleanPreferencesKey(key)] = value
        }
    }

    suspend fun saveUser(email: String, password: String) {
        putString(emailKey, email)
        putString(passwordKey, password)
        putBoolean(isLoggedKey, true)
    }

    suspend fun saveUserData(email: String, password: String, id: String, fullName: String, userName: String) {
        putString(emailKey, email)
        putString(passwordKey, password)
        putString(userIdKey, id)
        putString(fullNameKey, fullName)
        putString(userNameKey, userName)
    }

    suspend fun getToken(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[stringPreferencesKey(jwtKey)]
    }

    suspend fun getUserData(){
        val preferences = context.dataStore.data.first()
        val userName = preferences[stringPreferencesKey(userNameKey)] ?: ""
        val fullName = preferences[stringPreferencesKey(fullNameKey)] ?: ""
        val email = preferences[stringPreferencesKey(emailKey)] ?: ""
        val id = preferences[stringPreferencesKey(userIdKey)] ?: ""
        val photo = preferences[stringPreferencesKey(userPhotoUrl)] ?: ""
    }

    suspend fun isUserLogged(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[booleanPreferencesKey(isLoggedKey)] ?: false
    }

    suspend fun logOut() {
        context.dataStore.edit { editor -> editor.clear() }
    }


}