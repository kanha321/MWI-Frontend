package com.mwi.frontend.screens.upload

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.media3.exoplayer.ExoPlayer
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class UploadScreenModel(val fileUri: String) : ScreenModel {

    sealed class UploadStep {
        object Copying : UploadStep()
        object Preview : UploadStep()
        object Details : UploadStep()
        object Uploading : UploadStep()
        object Completed : UploadStep()
    }

    var currentStep by mutableStateOf<UploadStep>(UploadStep.Copying)
        private set

    fun nextStep() {
        currentStep = when (currentStep) {
            is UploadStep.Copying -> UploadStep.Preview
            is UploadStep.Preview -> UploadStep.Details
            is UploadStep.Details -> UploadStep.Uploading
            is UploadStep.Uploading -> UploadStep.Completed
            is UploadStep.Completed -> UploadStep.Completed
        }
    }

    fun previousStep() {
        currentStep = when (currentStep) {
            is UploadStep.Preview -> UploadStep.Copying
            is UploadStep.Details -> UploadStep.Preview
            is UploadStep.Uploading -> UploadStep.Details
            else -> currentStep
        }
    }




    var thumbnailUri by mutableStateOf<String?>(null)

    var extractFrameAt by mutableLongStateOf(5L * 1000) // Default to 5 seconds


    var copyProgress by mutableStateOf(0f)
        private set

    var fileSize by mutableStateOf(0L)
        private set

    var copiedBytes by mutableStateOf(0L)
        private set

    var fileType by mutableStateOf("")
        private set

    var transferSpeed by mutableStateOf(0L) // bytes per second
        private set

    var estimatedTimeRemaining by mutableStateOf(0L) // seconds
        private set

    private var startTime = 0L

    var isCopyingStarted by mutableStateOf(false)
        private set

    var isCopyingCompleted by mutableStateOf(false)
        private set

    var cachedVideoPath by mutableStateOf<String?>(null)
        private set

    fun updateCopyProgress(
        progress: Float,
        totalBytes: Long,
        copiedBytes: Long,
        fileType: String
    ) {
        this.copyProgress = progress
        this.fileSize = totalBytes
        this.copiedBytes = copiedBytes
        this.fileType = fileType

        // Calculate transfer speed
        val currentTime = System.currentTimeMillis()
        if (startTime == 0L) {
            startTime = currentTime
        }

        val elapsedTime = (currentTime - startTime) / 1000.0 // seconds
        if (elapsedTime > 0) {
            transferSpeed = (copiedBytes / elapsedTime).toLong()

            // Calculate ETA
            val remainingBytes = totalBytes - copiedBytes
            estimatedTimeRemaining = if (transferSpeed > 0) {
                remainingBytes / transferSpeed
            } else 0L
        }
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> "%.2f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576 -> "%.2f MB".format(bytes / 1_048_576.0)
            bytes >= 1024 -> "%.2f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    fun formatTransferSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond >= 1_073_741_824 -> "%.2f GB/s".format(bytesPerSecond / 1_073_741_824.0)
            bytesPerSecond >= 1_048_576 -> "%.2f MB/s".format(bytesPerSecond / 1_048_576.0)
            bytesPerSecond >= 1024 -> "%.2f KB/s".format(bytesPerSecond / 1024.0)
            else -> "$bytesPerSecond B/s"
        }
    }

    fun formatTime(seconds: Long): String {
        return when {
            seconds >= 3600 -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
            seconds >= 60 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }

    fun startCopying(context: Context, onSuccess: () -> Unit, onFailure: () -> Unit) {
        if (isCopyingStarted) return // Prevent multiple starts

        isCopyingStarted = true
        screenModelScope.launch {
            val result = copyUriToCacheFileWithProgress(
                context = context,
                uri = fileUri.toUri(),
                filename = "temp_video_${System.currentTimeMillis()}.mp4",
                onProgress = { progress, totalBytes, copiedBytes, fileType ->
                    updateCopyProgress(progress, totalBytes, copiedBytes, fileType)
                }
            )

            // Handle completion
            if (result != null) {
                cachedVideoPath = result.absolutePath
                isCopyingCompleted = true
                println("Copy completed: ${result.absolutePath}")
                onSuccess()
            } else {
                isCopyingStarted = false // Reset on failure to allow retry
                println("Copy failed")
                onFailure()
            }
        }
    }
    suspend fun copyUriToCacheFileWithProgress(
        context: Context,
        uri: Uri,
        filename: String,
        onProgress: (Float, Long, Long, String) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val tempFile = File(context.cacheDir, filename)
            val outputStream = FileOutputStream(tempFile)

            // Get file info
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            var totalBytes = 0L
            var mimeType = ""

            cursor?.use {
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst()) {
                    totalBytes = it.getLong(sizeIndex)
                    val fileName = it.getString(nameIndex)
                    mimeType = context.contentResolver.getType(uri) ?: "video/*"
                }
            }

            var copiedBytes = 0L
            val buffer = ByteArray(8192) // 8KB buffer

            inputStream.use { input ->
                outputStream.use { output ->
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        copiedBytes += bytesRead

                        // Calculate progress
                        val progress = if (totalBytes > 0) {
                            copiedBytes.toFloat() / totalBytes.toFloat()
                        } else 0f

                        // Update progress on main thread
                        withContext(Dispatchers.Main) {
                            onProgress(progress, totalBytes, copiedBytes, mimeType)
                        }
                    }
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    /**
     * Screen 2: Preview and Thumbnail Extraction
     */



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

//    suspend fun createDashFromUri(
//        context: Context,
//        videoUri: Uri,
//        durationMsForThumbnail: Long
//    ): List<File>? {
//        // Create a temporary file to hold the raw video input
//        val cachedFile = File(context.cacheDir, "temp_input_${System.currentTimeMillis()}.mp4")
//
//        try {
//            // 1. Copy the video from the Uri to a local cache file
//            if (copyUriToCacheFile(context, cachedFile.name) == null) {
//                return null
//            }
//
//            // 2. Convert the cached video file to DASH format
//            val mpdFile = convertVideoToDash(context, cachedFile) ?: return null
//
//            // 3. Get the output directory created by the DASH conversion
//            val outputDir = mpdFile.parentFile ?: return null
//
//            // 4. Extract the thumbnail and place it in the SAME directory
//            val thumbnailFile = extractThumbnailFromVideo(
//                inputFile = cachedFile,
//                outputDir = outputDir,
//            )
//
//            if (thumbnailFile == null) {
//                Log.e("Workflow", "Thumbnail extraction failed, but DASH conversion succeeded.")
//            }
//
//            // 5. Return a list of all files in the directory
//            return outputDir.listFiles()?.toList()
//        } finally {
//            // 6. Clean up the temporary input file in all cases
//            if (cachedFile.exists()) {
//                cachedFile.delete()
//            }
//        }
//    }

    /**
     * Copy To Cache
     */

    /**
     * Entry point function called when the user clicks the upload button.
     * This version uses a more robust method to separate the manifest from the segments.
     */
    suspend fun onUploadDashButtonClicked(
        allFiles: List<File>,
        title: String,
        description: String
    ) {
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
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("title", title)
                            append("description", description)
                            append("playlist", manifestFile.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, "application/dash+xml")
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "filename=\"${manifestFile.name}\""
                                )
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
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                batch.forEach { segmentFile ->
                                    append(
                                        "files",
                                        InputProvider { segmentFile.inputStream().asInput() },
                                        Headers.build {
                                            append(HttpHeaders.ContentType, "video/mp4")
                                            append(
                                                HttpHeaders.ContentDisposition,
                                                "filename=\"${segmentFile.name}\""
                                            )
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

    @SuppressLint("DefaultLocale")
    suspend fun extractThumbnailFromVideo(
        inputFile: File,
        outputDir: File,
    ): File? = withContext(Dispatchers.IO) {
        // This function no longer creates its own directory.
        // It saves the thumbnail in the provided outputDir.
        val outputJpg = File(outputDir, "thumbnail.jpg")
        val durationMs = extractFrameAt

        val timestamp = String.format(
            "%02d:%02d:%02d.%03d",
            TimeUnit.MILLISECONDS.toHours(durationMs),
            TimeUnit.MILLISECONDS.toMinutes(durationMs) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(durationMs) % TimeUnit.MINUTES.toSeconds(1),
            durationMs % 1000
        )

        val ffmpegCommand = arrayOf(
            "-ss", timestamp,
            "-i", inputFile.absolutePath,
            "-vframes", "1",
            "-q:v", "2",
            outputJpg.absolutePath
        )

        val session = FFmpegKit.execute(ffmpegCommand.joinToString(" "))

        if (ReturnCode.isSuccess(session.returnCode)) {
            outputJpg
        } else {
            // We don't delete the directory here as it might contain DASH files.
            null
        }
    }

    suspend fun extractFrameFromExoPlayer(
        context: Context,
        exoPlayer: ExoPlayer,
        timeInMillis: Long = extractFrameAt
    ): String? {
        return try {
            // Use the passed timeInMillis instead of current position
            withContext(Dispatchers.IO) {
                val outputFile =
                    File(context.cacheDir, "extracted_frame_${System.currentTimeMillis()}.png")

                // Use MediaMetadataRetriever to extract the frame
                val retriever = MediaMetadataRetriever()
                val timeUs = timeInMillis * 1000 // Use the saved position

                // Copy the video file to a temporary location for MediaMetadataRetriever
                val tempFile = File.createTempFile("temp_video_", ".mp4", context.cacheDir)
                val inputStream = context.contentResolver.openInputStream(fileUri.toUri())

                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                retriever.setDataSource(tempFile.absolutePath)
                val bitmap =
                    retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

                if (bitmap != null) {
                    // Save the bitmap to a file
                    outputFile.outputStream().use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    retriever.release()
                    tempFile.delete()
                    outputFile.absolutePath
                } else {
                    retriever.release()
                    tempFile.delete()
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}