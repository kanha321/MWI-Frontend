package com.kanhaji.basics.datastore

import platform.Foundation.NSUserDefaults

class IOSPreferencesDataStore : PreferencesDataStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun getString(key: String): String? = defaults.stringForKey(key)
    override suspend fun saveString(key: String, value: String) = defaults.setObject(value, key)
    override suspend fun deleteString(key: String) = defaults.removeObjectForKey(key)

    override suspend fun getInt(key: String): Int? = defaults.integerForKey(key).toInt()
    override suspend fun saveInt(key: String, value: Int) = defaults.setInteger(value.toLong(), key)
    override suspend fun deleteInt(key: String) = defaults.removeObjectForKey(key)

    override suspend fun getBoolean(key: String): Boolean? = defaults.boolForKey(key)
    override suspend fun saveBoolean(key: String, value: Boolean) = defaults.setBool(value, key)
    override suspend fun deleteBoolean(key: String) = defaults.removeObjectForKey(key)

    override suspend fun getDouble(key: String): Double? = defaults.doubleForKey(key)
    override suspend fun saveDouble(key: String, value: Double) = defaults.setDouble(value, key)
    override suspend fun deleteDouble(key: String) = defaults.removeObjectForKey(key)

    override suspend fun deleteAll() {
        val domain = NSUserDefaults.standardUserDefaults.persistentDomainForName("yourAppBundleId")
        domain?.keys?.forEach { key ->
            defaults.removeObjectForKey(key as String)
        }
    }
}

actual fun providePreferencesDataStore(): PreferencesDataStore = IOSPreferencesDataStore()