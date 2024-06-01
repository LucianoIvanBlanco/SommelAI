package com.blanco.somelai.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "SOMELAI_STORE")

class DataStoreManager(val context: Context) {

    private val emailKey = "EMAIL"
    private val passwordKey = "PASSWORD"
    private val jwtKey = "JWT"
    private val userNameKey = "USERNAME"
    private val fullNameKey = "FULLNAME"
    private val userPhotoUrl = "PHTOTO_URL"
    private val userIdKey = "USER_ID"
    private val isLoggedKey = "IS_LOGGED"


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

    suspend fun savedUserPhoto(photo: String) {
        putString(userPhotoUrl, photo)
    }
    suspend fun getToken(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[stringPreferencesKey(jwtKey)]
    }

    suspend fun getUserData(): Map<String, String> {
        val preferences = context.dataStore.data.first()
        val userName = preferences[stringPreferencesKey(userNameKey)] ?: ""
        val fullName = preferences[stringPreferencesKey(fullNameKey)] ?: ""
        val email = preferences[stringPreferencesKey(emailKey)] ?: ""
        val id = preferences[stringPreferencesKey(userIdKey)] ?: ""
        val photo = preferences[stringPreferencesKey(userPhotoUrl)] ?: ""
        val password = preferences[stringPreferencesKey(passwordKey)] ?: ""

        return mapOf(
            "userName" to userName,
            "fullName" to fullName,
            "email" to email,
            "id" to id,
            "photo" to photo,
            "password" to password
        )
    }

    suspend fun isUserLogged(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[booleanPreferencesKey(isLoggedKey)] ?: false
    }

    suspend fun logOut() {
        putBoolean(isLoggedKey, false)
    }

    suspend fun deleteUserData() {
        context.dataStore.edit { editor ->
            editor.remove(stringPreferencesKey(emailKey))
            editor.remove(stringPreferencesKey(passwordKey))
            editor.remove(stringPreferencesKey(userIdKey))
            editor.remove(stringPreferencesKey(fullNameKey))
            editor.remove(stringPreferencesKey(userNameKey))
            editor.remove(stringPreferencesKey(userPhotoUrl))
            editor.remove(booleanPreferencesKey(isLoggedKey))
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { editor ->
            editor.clear()
        }
    }
}