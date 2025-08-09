package com.kanhaji.basics.datastore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PreferencesDataStore {
    suspend fun getString(key: String): String?
    suspend fun saveString(key: String, value: String)
    suspend fun deleteString(key: String)

    suspend fun getInt(key: String): Int?
    suspend fun saveInt(key: String, value: Int)
    suspend fun deleteInt(key: String)

    suspend fun getBoolean(key: String): Boolean?
    suspend fun saveBoolean(key: String, value: Boolean)
    suspend fun deleteBoolean(key: String)

    suspend fun getDouble(key: String): Double?
    suspend fun saveDouble(key: String, value: Double)
    suspend fun deleteDouble(key: String)

    suspend fun deleteAll()
}

expect fun providePreferencesDataStore(): PreferencesDataStore

object PrefsManager {
    private var dataStore: PreferencesDataStore? = null
    private var initialized = false

    fun init() {
        dataStore = providePreferencesDataStore()
        initialized = true
    }

    private fun checkInit() {
        if (!initialized || dataStore == null) {
            throw IllegalStateException("PrefsManager is not initialized. Call PrefsManager.init() on each platform before use.")
        }
    }

    // String operations
    suspend fun getString(key: String): String? = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.getString(key)
    }

    suspend fun saveString(key: String, value: String) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.saveString(key, value)
    }

    suspend fun deleteString(key: String) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.deleteString(key)
    }

    // Int operations
    suspend fun getInt(key: String): Int? = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.getInt(key)
    }

    suspend fun saveInt(key: String, value: Int) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.saveInt(key, value)
    }

    suspend fun deleteInt(key: String) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.deleteInt(key)
    }

    // Boolean operations
    suspend fun getBoolean(key: String): Boolean? = withContext(Dispatchers.Default) {
        checkInit()
//        dataStore!!.getBoolean(key) == true
        dataStore?.getBoolean(key)
    }

    suspend fun saveBoolean(key: String, value: Boolean) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.saveBoolean(key, value)
    }

    suspend fun deleteBoolean(key: String) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.deleteBoolean(key)
    }

    // Double operations (now with proper platform support)
    suspend fun getDouble(key: String): Double? = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.getDouble(key)
    }

    suspend fun saveDouble(key: String, value: Double) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.saveDouble(key, value)
    }

    suspend fun deleteDouble(key: String) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.deleteDouble(key)
    }

    // Delete all preferences
    suspend fun deleteAll() = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.deleteAll()
    }
}