package com.blanco.somelai.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "SOMELAI_STORE")

class DataStoreManager(private val context: Context) {

    private val emailKey = stringPreferencesKey("EMAIL")
    private val passwordKey = stringPreferencesKey("PASSWORD")
    private val jwtKey = stringPreferencesKey("JWT")
    private val userNameKey = stringPreferencesKey("USERNAME")
    private val fullNameKey = stringPreferencesKey("FULLNAME")
    private val userPhotoUrl = stringPreferencesKey("PHOTO_URL")
    private val userIdKey = stringPreferencesKey("USER_ID")
    private val isLoggedKey = booleanPreferencesKey("IS_LOGGED")

    private suspend fun putString(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { editor ->
            editor[key] = value
        }
    }

    private suspend fun putBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { editor ->
            editor[key] = value
        }
    }

    suspend fun saveUser(email: String, password: String) {
        putString(emailKey, email)
        putString(passwordKey, password)
        putBoolean(isLoggedKey, true)
    }

    suspend fun saveUserData(email: String, password: String, id: String, fullName: String, userName: String) {
        context.dataStore.edit { editor ->
            editor[emailKey] = email
            editor[passwordKey] = password
            editor[userIdKey] = id
            editor[fullNameKey] = fullName
            editor[userNameKey] = userName
        }
    }

    suspend fun saveUserPhoto(photo: String) {
        putString(userPhotoUrl, photo)
    }

    suspend fun getToken(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[jwtKey]
    }

    fun getUserData(): Flow<Map<String, String>> {
        return context.dataStore.data.map { preferences ->
            mapOf(
                "userName" to (preferences[userNameKey] ?: ""),
                "fullName" to (preferences[fullNameKey] ?: ""),
                "email" to (preferences[emailKey] ?: ""),
                "id" to (preferences[userIdKey] ?: ""),
                "photo" to (preferences[userPhotoUrl] ?: ""),
                "password" to (preferences[passwordKey] ?: "")
            )
        }
    }

    suspend fun isUserLogged(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[isLoggedKey] ?: false
    }

    suspend fun logOut() {
        context.dataStore.edit { editor ->
            editor[isLoggedKey] = false
        }
    }

    suspend fun deleteUserData() {
        context.dataStore.edit { editor ->
            editor.remove(emailKey)
            editor.remove(passwordKey)
            editor.remove(userIdKey)
            editor.remove(fullNameKey)
            editor.remove(userNameKey)
            editor.remove(userPhotoUrl)
            editor.remove(isLoggedKey)
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { editor ->
            editor.clear()
        }
    }
}
