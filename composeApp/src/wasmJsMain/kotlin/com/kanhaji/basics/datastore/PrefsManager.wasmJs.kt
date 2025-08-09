package com.kanhaji.basics.datastore

import kotlinx.browser.localStorage

// In wasmJsMain source set
actual fun providePreferencesDataStore(): PreferencesDataStore {
    return WebPreferencesDataStore()
}

class WebPreferencesDataStore : PreferencesDataStore {
    override suspend fun getString(key: String): String? {
        return localStorage.getItem(key)
    }

    override suspend fun saveString(key: String, value: String) {
        localStorage.setItem(key, value)
    }

    override suspend fun deleteString(key: String) {
        localStorage.removeItem(key)
    }

    override suspend fun getInt(key: String): Int? {
        return localStorage.getItem(key)?.toIntOrNull()
    }

    override suspend fun saveInt(key: String, value: Int) {
        localStorage.setItem(key, value.toString())
    }

    override suspend fun deleteInt(key: String) {
        localStorage.removeItem(key)
    }

    override suspend fun getBoolean(key: String): Boolean? {
        return localStorage.getItem(key)?.toBooleanStrictOrNull()
    }

    override suspend fun saveBoolean(key: String, value: Boolean) {
        localStorage.setItem(key, value.toString())
    }

    override suspend fun deleteBoolean(key: String) {
        localStorage.removeItem(key)
    }

    override suspend fun getDouble(key: String): Double? {
        return localStorage.getItem(key)?.toDoubleOrNull()
    }

    override suspend fun saveDouble(key: String, value: Double) {
        localStorage.setItem(key, value.toString())
    }

    override suspend fun deleteDouble(key: String) {
        localStorage.removeItem(key)
    }

    override suspend fun deleteAll() {
        localStorage.clear()
    }
}