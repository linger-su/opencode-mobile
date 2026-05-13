package com.opencode.mobile.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val PROVIDER_KEY = stringPreferencesKey("provider")
        private val API_KEY_KEY = stringPreferencesKey("api_key")
        private val BASE_URL_KEY = stringPreferencesKey("base_url")
        private val MODEL_KEY = stringPreferencesKey("model")
        private val PC_SERVER_URL_KEY = stringPreferencesKey("pc_server_url")

        // 默认配置 - 空值，需要用户在设置中配置
        const val DEFAULT_PROVIDER = "openai"
        const val DEFAULT_API_KEY = ""
        const val DEFAULT_BASE_URL = ""
        const val DEFAULT_MODEL = ""
    }

    val provider: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PROVIDER_KEY] ?: DEFAULT_PROVIDER
    }

    val apiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_KEY_KEY] ?: DEFAULT_API_KEY
    }

    val baseUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BASE_URL_KEY] ?: DEFAULT_BASE_URL
    }

    val model: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[MODEL_KEY] ?: DEFAULT_MODEL
    }

    val pcServerUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PC_SERVER_URL_KEY] ?: "http://localhost:4096"
    }

    suspend fun setProvider(provider: String) {
        context.dataStore.edit { preferences ->
            preferences[PROVIDER_KEY] = provider
        }
    }

    suspend fun setApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY_KEY] = apiKey
        }
    }

    suspend fun setBaseUrl(baseUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[BASE_URL_KEY] = baseUrl
        }
    }

    suspend fun setModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[MODEL_KEY] = model
        }
    }

    suspend fun setPcServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PC_SERVER_URL_KEY] = url
        }
    }
}
