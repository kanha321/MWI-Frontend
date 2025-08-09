package com.kanhaji.basics.datastore

import com.kanhaji.basics.util.Resources
import com.kanhaji.basics.util.Decryption
import com.kanhaji.basics.util.Encryption
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Properties

class DesktopPreferencesDataStore : PreferencesDataStore {
    private val mutex = Mutex()
    private val file: File by lazy {
        val userHome = System.getProperty("user.home")
        val configDir = File(userHome, Resources.DESKTOP_FOLDER)
        if (!configDir.exists()) configDir.mkdirs()
        val propsFile = File(configDir, Resources.PREFS_NAME)
        if (!propsFile.exists()) {
            propsFile.createNewFile()
            saveProperties(Properties())
        }
        propsFile
    }

    private fun loadProperties(): Properties {
        val props = Properties()
        val decrypted = Decryption.readAndValidateFile(file.absolutePath) ?: ""
        if (decrypted.isNotBlank()) {
            props.load(decrypted.byteInputStream())
        }
        return props
    }

    private fun saveProperties(props: Properties) {
        val outBytes = ByteArrayOutputStream().use { baos ->
            props.store(baos, null)
            baos.toByteArray()
        }
        val asString = String(outBytes)
        Encryption.writeSelfHealingFile(file.absolutePath, asString)
    }

    override suspend fun getString(key: String): String? = mutex.withLock {
        loadProperties().getProperty(key)
    }

    override suspend fun saveString(key: String, value: String) = mutex.withLock {
        val props = loadProperties()
        props.setProperty(key, value)
        saveProperties(props)
    }

    override suspend fun deleteString(key: String) = mutex.withLock {
        val props = loadProperties()
        props.remove(key)
        saveProperties(props)
    }

    override suspend fun getInt(key: String): Int? = mutex.withLock {
        loadProperties().getProperty(key)?.toIntOrNull()
    }

    override suspend fun saveInt(key: String, value: Int) = saveString(key, value.toString())

    override suspend fun deleteInt(key: String) = deleteString(key)

    override suspend fun getBoolean(key: String): Boolean? = mutex.withLock {
        loadProperties().getProperty(key)?.toBooleanStrictOrNull()
    }

    override suspend fun saveBoolean(key: String, value: Boolean) = saveString(key, value.toString())

    override suspend fun deleteBoolean(key: String) = deleteString(key)

    override suspend fun getDouble(key: String): Double? = mutex.withLock {
        loadProperties().getProperty(key)?.toDoubleOrNull()
    }

    override suspend fun saveDouble(key: String, value: Double) = saveString(key, value.toString())

    override suspend fun deleteDouble(key: String) = deleteString(key)

    override suspend fun deleteAll() = mutex.withLock {
        saveProperties(Properties())
    }
}

actual fun providePreferencesDataStore(): PreferencesDataStore = DesktopPreferencesDataStore()