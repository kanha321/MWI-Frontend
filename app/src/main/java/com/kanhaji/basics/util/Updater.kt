package com.kanhaji.basics.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.kanhaji.basics.networking.httpClient
import com.mwi.frontend.entity.Update
import com.mwi.frontend.util.AppSettingsItems.isUpdateAvailable
import com.mwi.frontend.util.MwiUtils
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object Updater {

    var downloadProgress by mutableFloatStateOf(0f)
    var isDownloading by mutableStateOf<Boolean?>(null)
    var updateChecked by mutableStateOf(false)
    var update by mutableStateOf<Update?>(null)

    suspend fun checkForUpdates() {
        val url = MwiUtils.BASE_URL + "/api/update/info"
        return try {
            val response = httpClient.get(url)
            val update = response.body<Update>()
            if (update.latestVersionCode > MwiUtils.appVersionCode) isUpdateAvailable = true
            this.update = update
            updateChecked = true
        } catch (e: Exception) {
            e.printStackTrace()
            this.update = null
            updateChecked = true
        }
    }

    suspend fun startDownload(
        context: Context,
        onDownloadComplete: (File) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (update == null) {
            println("No update data available.")
            return@withContext
        }

        isDownloading = true
        downloadProgress = 0f

        val downloadUrl = MwiUtils.BASE_URL + update!!.downloadUrl

        try {
            val response: HttpResponse = httpClient.get(downloadUrl) {
                timeout {
                    requestTimeoutMillis = 600_000 // 10 min
                    connectTimeoutMillis = 30_000  // 30 sec
                    socketTimeoutMillis = 600_000  // 10 min
                }
            }

            val contentLength = response.contentLength() ?: -1L
            val fileName = downloadUrl.substringAfterLast("/")
            val outputFile = File(context.cacheDir, fileName)

            outputFile.outputStream().use { fos ->
                val channel: ByteReadChannel = response.bodyAsChannel()
                var bytesRead = 0L
                val buffer = ByteArray(8192)

                while (!channel.isClosedForRead) {
                    val read = channel.readAvailable(buffer, 0, buffer.size)
                    if (read == -1) break
                    fos.write(buffer, 0, read)

                    bytesRead += read
                    if (contentLength > 0) {
                        val progress = bytesRead.toFloat() / contentLength.toFloat()
                        withContext(Dispatchers.Main) {
                            downloadProgress = progress
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                downloadProgress = 1f
                isDownloading = false
            }

            println("✅ Download complete: ${outputFile.absolutePath}")
            withContext(Dispatchers.Main) {
                onDownloadComplete(outputFile)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                downloadProgress = 0f
                isDownloading = false
            }
        }
    }

}
