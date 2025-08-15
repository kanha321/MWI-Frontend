package com.mwi.frontend.screens.upload

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.kanhaji.basics.networking.httpClient
import com.mwi.frontend.util.FileType
import com.mwi.frontend.util.Resources
import io.ktor.client.call.body
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

class UploadScreenModel : ScreenModel {

    var videoUri by mutableStateOf<String?>(null)
    var thumbnailUri by mutableStateOf<String?>(null)
    // filePicker
    suspend fun openFilePicker(context: Context, type: FileType): String? =
        suspendCancellableCoroutine { continuation ->
            val activity = getCurrentActivity(context) as? ComponentActivity ?: run {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val launcher = activity.activityResultRegistry.register(
                "file_picker",
                ActivityResultContracts.GetContent()
            ) { uri ->
                continuation.resume(uri?.toString())
            }
            launcher.launch(type.mimeType)
        }

    private fun getCurrentActivity(context: Context): Activity? {
        return context as Activity? ?: run {
            // If the app context is not an Activity, we cannot open the file picker
            null
        }
    }



    /**
    * Video Conversion to DASH
    */
    suspend fun convertVideoToDash(context: Context, inputFile: File): File? =
        withContext(Dispatchers.IO) {
            val outputDir = File(
                context.cacheDir,
                "dash_${inputFile.nameWithoutExtension}_${System.currentTimeMillis()}"
            )
            if (!outputDir.exists()) outputDir.mkdirs()
            val outputMpd = File(outputDir, "index.mpd")

            val ffmpegCommand = arrayOf(
                "-i", inputFile.absolutePath,
                "-c", "copy",
                "-f", "dash",
                "-seg_duration", "4",
                "-use_template", "1",
                "-use_timeline", "1",
                outputMpd.absolutePath
            )

            val session = FFmpegKit.execute(ffmpegCommand.joinToString(" "))
            val returnCode = session.returnCode

            if (ReturnCode.isSuccess(returnCode)) {
                outputMpd
            } else {
                outputDir.deleteRecursively()
                null
            }
        }

    suspend fun createDashFromUri(context: Context, videoUri: Uri): List<File>? {
        val cachedFile = File.createTempFile("temp_dash_input_", ".mp4", context.cacheDir)

        return try {
            if (copyUriToCacheFile(context, videoUri, cachedFile.name) == null) {
                return null
            }

            val mpdFile = convertVideoToDash(context, cachedFile) ?: return null
            val outputDir = mpdFile.parentFile ?: return null
            val dashFiles = outputDir.listFiles()?.toList()

            dashFiles
        } finally {
            if (cachedFile.exists()) {
                cachedFile.delete()
            }
        }
    }

    /**
     * Copy To Cache
     */
    fun copyUriToCacheFile(context: Context, uri: Uri, filename: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, filename)
            val outputStream = FileOutputStream(tempFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }




    /**
     * Entry point function called when the user clicks the upload button.
     * This version uses a more robust method to separate the manifest from the segments.
     */
    suspend fun onUploadDashButtonClicked(allFiles: List<File>, title: String, description: String) {
        // This function now runs on the thread of its caller.
        // The 'uploadDashVideoInBatches' function should be called from a coroutine.
        // Example: scope.launch(Dispatchers.IO) { onUploadDashButtonClicked(...) }

        // IMPROVEMENT: Find the manifest, then treat everything else as a segment.
        // This avoids hardcoding the ".m4s" extension and is more robust.
        val manifestFile = allFiles.firstOrNull { it.extension == "mpd" }
        val segmentFiles = allFiles.filter { it != manifestFile }

        if (manifestFile == null || segmentFiles.isEmpty()) {
            // IMPROVEMENT: More generic error message.
            println("Error: Manifest (.mpd) or segment files not found in the list.")
            // Show error to user
            return
        }

        // Call the DASH upload function, which remains unchanged.
        // Make sure this is called from a coroutine scope, e.g., scope.launch { ... }
        val result = uploadDashVideoInBatches(manifestFile, segmentFiles, title, description)

        // Handle the result on the main thread if updating UI
        result.onSuccess { successMessage ->
            println(successMessage)
            // e.g., withContext(Dispatchers.Main) { show success UI }
        }.onFailure { exception ->
            println("Upload failed: ${exception.message}")
            // e.g., withContext(Dispatchers.Main) { show error UI }
        }
    }


    /**
     * The core upload function.
     * NO CHANGES ARE NEEDED HERE. It correctly accepts the separated manifest and segment files.
     */
    suspend fun uploadDashVideoInBatches(
        manifestFile: File,
        segmentFiles: List<File>,
        title: String,
        description: String
    ): Result<String> {
        // ... same implementation as before
        val url = "${Resources.BASE_URL}/api/videos"

        try {
            // STEP 1: Create video record and upload manifest
            println("Step 1: Creating video record and uploading DASH manifest...")
            val videoId = httpClient.post("$url/create") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("title", title)
                        append("description", description)
                        append("playlist", manifestFile.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, "application/dash+xml")
                            append(HttpHeaders.ContentDisposition, "filename=\"${manifestFile.name}\"")
                        })
                    }
                ))
            }.body<String>()

            if (videoId.isBlank()) {
                return Result.failure(Exception("Failed to create video record: Server returned an empty ID."))
            }
            println("Step 1 SUCCESS. Received videoId: $videoId")


            // STEP 2: Upload segments using streaming to save memory
            println("Step 2: Uploading segments in batches...")
            val batchSize = 25
            val segmentBatches = segmentFiles.chunked(batchSize)

            segmentBatches.forEachIndexed { index, batch ->
                println("Uploading batch ${index + 1} of ${segmentBatches.size}...")
                httpClient.post("$url/$videoId/segments") {
                    setBody(MultiPartFormDataContent(
                        formData {
                            batch.forEach { segmentFile ->
                                append("files", InputProvider { segmentFile.inputStream().asInput() }, Headers.build {
                                    append(HttpHeaders.ContentType, "video/mp4")
                                    append(HttpHeaders.ContentDisposition, "filename=\"${segmentFile.name}\"")
                                })
                            }
                        }
                    ))
                }
            }
            println("Step 2 SUCCESS. All segment batches uploaded.")


            // STEP 3: Finalize (This step is usually format-agnostic and needs no changes)
            println("Step 3: Finalizing upload...")
            httpClient.post("$url/$videoId/finalize")
            println("Step 3 SUCCESS. Video is marked as ready.")

            return Result.success("Upload complete! Video ID: $videoId")

        } catch (e: Exception) {
            println("Upload failed during the process: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }
}