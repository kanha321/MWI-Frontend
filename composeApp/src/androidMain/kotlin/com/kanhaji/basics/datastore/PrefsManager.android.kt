package com.kanhaji.basics.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.kanhaji.basics.util.Resources
import com.mwi.frontend.AndroidContext
import kotlinx.coroutines.flow.first

private var DATASTORE_NAME = Resources.PREFS_NAME
private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class AndroidPreferencesDataStore(private val context: Context) : PreferencesDataStore {

    override suspend fun getString(key: String): String? {
        val prefsKey = stringPreferencesKey(key)
        val prefs = context.dataStore.data.first()
        return prefs[prefsKey]
    }

    override suspend fun saveString(key: String, value: String) {
        val prefsKey = stringPreferencesKey(key)
        context.dataStore.edit { it[prefsKey] = value }
    }

    override suspend fun deleteString(key: String) {
        val prefsKey = stringPreferencesKey(key)
        context.dataStore.edit { it.remove(prefsKey) }
    }

    override suspend fun getInt(key: String): Int? {
        val prefsKey = intPreferencesKey(key)
        val prefs = context.dataStore.data.first()
        return prefs[prefsKey]
    }

    override suspend fun saveInt(key: String, value: Int) {
        val prefsKey = intPreferencesKey(key)
        context.dataStore.edit { it[prefsKey] = value }
    }

    override suspend fun deleteInt(key: String) {
        val prefsKey = intPreferencesKey(key)
        context.dataStore.edit { it.remove(prefsKey) }
    }

    override suspend fun getBoolean(key: String): Boolean? {
        val prefsKey = booleanPreferencesKey(key)
        val prefs = context.dataStore.data.first()
        return prefs[prefsKey]
    }

    override suspend fun saveBoolean(key: String, value: Boolean) {
        val prefsKey = booleanPreferencesKey(key)
        context.dataStore.edit { it[prefsKey] = value }
    }

    override suspend fun deleteBoolean(key: String) {
        val prefsKey = booleanPreferencesKey(key)
        context.dataStore.edit { it.remove(prefsKey) }
    }

    override suspend fun getDouble(key: String): Double? {
        val prefsKey = doublePreferencesKey(key)
        val prefs = context.dataStore.data.first()
        return prefs[prefsKey]
    }

    override suspend fun saveDouble(key: String, value: Double) {
        val prefsKey = doublePreferencesKey(key)
        context.dataStore.edit { it[prefsKey] = value }
    }

    override suspend fun deleteDouble(key: String) {
        val prefsKey = doublePreferencesKey(key)
        context.dataStore.edit { it.remove(prefsKey) }
    }

    override suspend fun deleteAll() {
        context.dataStore.edit { it.clear() }
    }
}

actual fun providePreferencesDataStore(): PreferencesDataStore {
    return AndroidPreferencesDataStore(AndroidContext.appContext)
}