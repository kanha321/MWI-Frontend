package com.mwi.frontend.screens.upload

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.Statistics
import com.kanhaji.basics.networking.httpClient
import com.mwi.frontend.entity.CreateVideoForm
import com.mwi.frontend.entity.DashBuildResult
import com.mwi.frontend.util.MwiUtils
import com.mwi.frontend.util.reduceSpaces
import io.ktor.client.call.body
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.utils.io.streams.asInput
import java.io.IOException
import java.util.Locale
import java.util.concurrent.CancellationException
import kotlin.math.min

class UploadScreenModel(val fileUri: String) : ScreenModel {

    private fun uploadCacheDir(context: Context): File =
        File(context.cacheDir, "UploadCache").apply { if (!exists()) mkdirs() }

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


    var copyProgress by mutableFloatStateOf(0f)
        private set

    var fileSize by mutableLongStateOf(0L)
        private set

    var copiedBytes by mutableLongStateOf(0L)
        private set

    var fileType by mutableStateOf("")
        private set

    var transferSpeed by mutableLongStateOf(0L) // bytes per second
        private set

    var estimatedTimeRemaining by mutableLongStateOf(0L) // seconds
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
            val inputStream =
                context.contentResolver.openInputStream(uri) ?: return@withContext null
            val tempFile = File(uploadCacheDir(context), filename)
            val outputStream = FileOutputStream(tempFile)

            // Get file info
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            var totalBytes = 0L
            var mimeType = ""

            cursor?.use {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
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

    var videoDuration by mutableLongStateOf(0L) // Duration in milliseconds
        private set


    suspend fun extractFrameWithFfmpeg(
        context: Context,
        timeInMillis: Long
    ): String? = withContext(Dispatchers.IO) {
        val inputPath = cachedVideoPath ?: return@withContext null
        try {
            val outDir = uploadCacheDir(context)
            val outputFile = File(outDir, "extracted_frame_${timeInMillis}.png")
            val ts = formatMsForFfmpeg(min(timeInMillis, videoDuration - 100))
//            Toast.makeText(
//                context,
//                "Extracting at $timeInMillis",
//                Toast.LENGTH_SHORT
//            ).show()
            println("timeInMillis: $timeInMillis, formatted: $ts, total duration: $videoDuration")

            // Build FFmpeg command: seek -> read -> grab one frame -> save as PNG
            val cmd = listOf(
                "-hide_banner",
                "-loglevel", "error",
                "-y",
                "-ss", ts,
                "-i", inputPath,
                "-frames:v", "1",
                "-q:v", "2",
                outputFile.absolutePath
            ).joinToString(" ")

            val session = FFmpegKit.execute(cmd)
            if (ReturnCode.isSuccess(session.returnCode)) {
                println("thumbnail output path: ${outputFile.absolutePath}")
                outputFile.absolutePath
            } else {
                println("thumbnail output path: ${outputFile.absolutePath}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Helper to format milliseconds to ffmpeg timestamp (HH:mm:ss.SSS)
    @SuppressLint("DefaultLocale")
    private fun formatMsForFfmpeg(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = (totalSeconds % 60)
        val millis = ms % 1000
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
    }


    /**
     *  Title and Description
     */

    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var nsfw by mutableStateOf(true)

    fun canContinueToUpload(): Boolean {
        return title.isNotBlank() && description.isNotBlank() && cachedVideoPath != null && thumbnailUri != null
    }


    /**
     * Screen 4: Getting remaining Data and finalize
     */

    private suspend fun getVideoDurationMsWithFfmpeg(): Long = withContext(Dispatchers.IO) {
        val path = cachedVideoPath ?: return@withContext 0L
        runCatching {
            val session = FFprobeKit.getMediaInformation(path)
            val info = session.mediaInformation
            val seconds = info?.duration?.toDoubleOrNull() ?: 0.0
            (seconds * 1000.0).toLong()
        }.getOrDefault(0L)
    }

    fun setVideoDurationWithFfmpeg () {
        screenModelScope.launch {
            videoDuration = getVideoDurationMsWithFfmpeg()
        }
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"
    }

    suspend fun buildDashFromCache(
        context: Context,
        segmentDurationSec: Int = 4
    ): DashBuildResult? = withContext(Dispatchers.IO) {
        val inputPath = cachedVideoPath ?: return@withContext null
        val thumbRef = thumbnailUri ?: return@withContext null

        // Prepare output directory inside UploadCache
        val inputFile = File(inputPath)
        val outDir = File(
            uploadCacheDir(context),
            "dash_${inputFile.nameWithoutExtension}_${System.currentTimeMillis()}"
        )
        if (!outDir.exists() && !outDir.mkdirs()) return@withContext null

        val manifestFile = File(outDir, "index.mpd")

        // Build a simple single‑representation DASH package (copy streams if possible)
        val ffCmd = listOf(
            "-hide_banner",
//            "-loglevel", "error",
            "-y",
            "-i", inputPath,
            "-map", "0:v:0",
            "-map", "0:a:0?",
            "-c:v", "copy",
            "-c:a", "copy",
            "-f", "dash",
            "-seg_duration", segmentDurationSec.toString(),
            "-use_template", "1",
            "-use_timeline", "1",
            "-init_seg_name", "init_${'$'}RepresentationID$.${'$'}ext$",
            "-media_seg_name", "chunk_${'$'}RepresentationID${'$'}_${'$'}Number%05d$.${'$'}ext$",
            manifestFile.absolutePath
        ).joinToString(" ")

        try {
            val dashSession = FFmpegKit.execute(ffCmd)
            if (!ReturnCode.isSuccess(dashSession.returnCode)) {
                outDir.deleteRecursively()
                return@withContext null
            }

            val durationMs = getVideoDurationMsWithFfmpeg()

            // Resolve thumbnail source file from thumbnailUri (can be path, file://, or content://)
            val thumbSrcFile: File? = when {
                thumbRef.startsWith("content://") -> {
                    val uri = thumbRef.toUri()
                    // Try to find an existing cached file first
                    getFileAtCacheFromUri(context, uri) ?: run {
                        // Fallback: copy content URI directly into outDir
                        val name = queryDisplayName(context, uri) ?: "thumbnail.png"
                        val dest = File(outDir, name)
                        if (copyContentUriToFile(context, uri, dest)) dest else null
                    }
                }

                thumbRef.startsWith("file://") -> {
                    thumbRef.toUri().path?.let(::File)
                }

                else -> {
                    // Assume it's a direct absolute path returned by previous extraction
                    File(thumbRef)
                }
            }

            if (thumbSrcFile == null || !thumbSrcFile.exists()) {
                outDir.deleteRecursively()
                return@withContext null
            }

            // Ensure thumbnail resides in the same outDir (copy only if needed)
            val thumbDest = if (thumbSrcFile.parentFile?.absolutePath == outDir.absolutePath) {
                thumbSrcFile
            } else {
                val dest = File(outDir, thumbSrcFile.name)
                thumbSrcFile.copyTo(dest, overwrite = true)
                dest
            }

            val allFiles = outDir.listFiles()?.toList().orEmpty()
            val segments = allFiles.filter { it != manifestFile && it != thumbDest }

            DashBuildResult(
                outputDir = outDir,
                manifest = manifestFile,
                segments = segments,
                thumbnail = thumbDest,
                durationMs = durationMs
            )
        } catch (e: Exception) {
            e.printStackTrace()
            outDir.deleteRecursively()
            null
        }
    }


    var dashPhase by mutableStateOf("Idle")
        private set
    var dashProgress by mutableFloatStateOf(0f) // 0.0..1.0
    var dashSpeedX by mutableStateOf("0x")
        private set
    var dashElapsedMs by mutableLongStateOf(0L)
        private set
    var dashEtaMs by mutableLongStateOf(0L)
        private set
    var dashDurationMs by mutableLongStateOf(0L)
        private set
    val dashLogs = mutableStateListOf<String>() // bounded console
    private var lastStatTimeMs = 0L

    private fun clearDashTelemetry() {
        dashPhase = "Preparing"
        dashProgress = 0f
        dashSpeedX = "0x"
        dashElapsedMs = 0L
        dashEtaMs = 0L
        lastStatTimeMs = 0L
        dashLogs.clear()
    }

    private fun appendDashLog(line: String) {
        // Keep last ~200 lines
        if (dashLogs.size >= 200) dashLogs.removeAt(0)
        dashLogs.add(line)
    }

    // Parse e.g.: "size=N/A time=00:00:38.93 bitrate=N/A speed= 774x"
    private fun parseLogProgressLine(line: String, durationMs: Long) {
        val timeRegex = Regex("""time=(\d{2}):(\d{2}):(\d{2}\.\d{2})""")
        val speedRegex = Regex("""speed=\s*([0-9.]+)x""")

        val timeMatch = timeRegex.find(line)
        val speedMatch = speedRegex.find(line)

        // Numeric speed: prefer current line, else last known from dashSpeedX
        val speedX = speedMatch?.groupValues?.getOrNull(1)?.toDoubleOrNull()
            ?: dashSpeedX.removeSuffix("x").trim().toDoubleOrNull()

        if (timeMatch != null) {
            val h = timeMatch.groupValues[1].toInt()
            val m = timeMatch.groupValues[2].toInt()
            val sFloat = timeMatch.groupValues[3].toDouble()
            val sec = h * 3600 + m * 60 + sFloat
            val processedMs = (sec * 1000).toLong() // media time processed

            dashDurationMs = durationMs
            if (durationMs > 0) {
                dashProgress = (processedMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

                // Wall-clock elapsed and ETA using speed
                if (speedX != null && speedX > 0.0) {
                    val elapsedWallMs = (processedMs / speedX).toLong()
                    dashElapsedMs = elapsedWallMs.coerceAtLeast(dashElapsedMs)

                    val remainingMediaMs = (durationMs - processedMs).coerceAtLeast(0)
                    dashEtaMs = (remainingMediaMs / speedX).toLong()
                } else {
                    // Fallback: naive (treat media time as wall time)
                    dashElapsedMs = processedMs.coerceAtLeast(dashElapsedMs)
                    dashEtaMs = (durationMs - processedMs).coerceAtLeast(0)
                }
            }
        }

        if (speedMatch != null) {
            dashSpeedX = "${speedMatch.groupValues[1]}x"
        }
    }

    // Statistics callback is more reliable than parsing logs for time/size.
    private fun onStatistics(stats: Statistics, durationMs: Long) {
        val processedMs = stats.time.toLong() // media time processed (ms)
        if (processedMs >= lastStatTimeMs) {
            lastStatTimeMs = processedMs
            dashDurationMs = durationMs

            val speedNum = stats.speed

            if (durationMs > 0) {
                dashProgress = (processedMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

                if (speedNum > 0.0) {
                    val elapsedWallMs = (processedMs / speedNum).toLong()
                    dashElapsedMs = elapsedWallMs.coerceAtLeast(dashElapsedMs)

                    val remainingMediaMs = (durationMs - processedMs).coerceAtLeast(0)
                    dashEtaMs = (remainingMediaMs / speedNum).toLong()

                    dashSpeedX = String.format(Locale.US, "%.2fx", speedNum)
                } else {
                    // Fallback if speed is unavailable
                    dashElapsedMs = processedMs.coerceAtLeast(dashElapsedMs)
                    dashEtaMs = (durationMs - processedMs).coerceAtLeast(0)
                }
            }
        }
    }
    // Public API to start building DASH with live progress suitable for UI.
// Add these properties to track DASH conversion state
    var dashSessionKey by mutableStateOf<String?>(null)
    var dashResult by mutableStateOf<DashBuildResult?>(null)
        private set

    // Update buildDashFromCacheWithUi to check for existing session
    suspend fun buildDashFromCacheWithUi(
        context: Context,
        segmentDurationSec: Int = 4,
        onSuccess: (DashBuildResult) -> Unit = {},
        onFailure: (Throwable?) -> Unit = {}
    ): DashBuildResult? = withContext(Dispatchers.IO) {
        val inputPath = cachedVideoPath ?: run {
            screenModelScope.launch(Dispatchers.Main) {
                onFailure(IllegalStateException("No cached video available"))
            }
            return@withContext null
        }

        val currentSessionKey = inputPath

        // Return existing result if DASH already built for this session
        dashResult?.let { result ->
            if (dashSessionKey == currentSessionKey) {
                println("[DASH] Session already completed: $currentSessionKey")
                screenModelScope.launch(Dispatchers.Main) {
                    onSuccess(result)
                }
                return@withContext result
            }
        }

        // Prevent multiple DASH builds for the same session
        if (dashPhase == "Packaging" && dashSessionKey == currentSessionKey) {
            println("[DASH] Build already in progress for session: $currentSessionKey")
            return@withContext null
        }

        // Set session key to track this DASH build
        dashSessionKey = currentSessionKey

        val thumbRef = thumbnailUri ?: run {
            screenModelScope.launch(Dispatchers.Main) {
                onFailure(IllegalStateException("No thumbnail selected"))
            }
            return@withContext null
        }

        // Rest of your existing implementation...
        val durationMs = getVideoDurationMsWithFfmpeg().coerceAtLeast(0L)

        val inputFile = File(inputPath)
        val outDir = File(
            uploadCacheDir(context),
            "dash_${inputFile.nameWithoutExtension}_${System.currentTimeMillis()}"
        )
        if (!outDir.exists() && !outDir.mkdirs()) {
            screenModelScope.launch(Dispatchers.Main) {
                onFailure(IOException("Failed to create output directory"))
            }
            return@withContext null
        }
        val manifestFile = File(outDir, "index.mpd")

        val ffCmd = listOf(
            "-hide_banner",
            "-loglevel",
            "info",
            "-y",
            "-i",
            inputPath,
            "-map",
            "0:v:0",
            "-map",
            "0:a:0?",
            "-c:v",
            "copy",
            "-c:a",
            "copy",
            "-f",
            "dash",
            "-seg_duration",
            segmentDurationSec.toString(),
            "-use_template",
            "1",
            "-use_timeline",
            "1",
            "-init_seg_name",
            "init_${'$'}RepresentationID$.${'$'}ext$",
            "-media_seg_name",
            "chunk_${'$'}RepresentationID${'$'}_${'$'}Number%05d$.${'$'}ext$",
            manifestFile.absolutePath
        ).joinToString(" ")

        clearDashTelemetry()
        dashPhase = "Packaging"

        suspendCancellableCoroutine<DashBuildResult?> { cont ->
            val session = FFmpegKit.executeAsync(
                ffCmd,
                { completedSession ->
                    val success = ReturnCode.isSuccess(completedSession.returnCode)
                    screenModelScope.launch(Dispatchers.Main) {
                        dashProgress = if (success) 1f else dashProgress
                        dashPhase = if (success) "Completed" else "Failed"
                    }

                    if (success) {
                        try {
                            val thumbDest = copyThumbnailTo(outDir, thumbRef, context)
                            val allFiles = outDir.listFiles()?.toList().orEmpty()
                            val segments = allFiles.filter { it != manifestFile && it != thumbDest }

                            val result = DashBuildResult(
                                outputDir = outDir,
                                manifest = manifestFile,
                                segments = segments,
                                thumbnail = thumbDest,
                                durationMs = durationMs
                            )

                            // Store the result for future use
                            dashResult = result

                            screenModelScope.launch(Dispatchers.Main) {
                                onSuccess(result)
                            }
                            cont.resume(result)
                        } catch (e: Exception) {
                            outDir.deleteRecursively()
                            screenModelScope.launch(Dispatchers.Main) {
                                onFailure(e)
                            }
                            cont.resume(null)
                        }
                    } else {
                        outDir.deleteRecursively()
                        screenModelScope.launch(Dispatchers.Main) {
                            onFailure(null)
                        }
                        cont.resume(null)
                    }
                },
                { log ->
                    val line = log.message
                    screenModelScope.launch(Dispatchers.Main) {
                        appendDashLog(line)
                        parseLogProgressLine(line, durationMs)
                    }
                },
                { stats ->
                    screenModelScope.launch(Dispatchers.Main) {
                        onStatistics(stats, durationMs)
                    }
                }
            )

            cont.invokeOnCancellation {
                session.cancel()
                screenModelScope.launch(Dispatchers.Main) {
                    onFailure(CancellationException("Packaging cancelled"))
                }
            }
        }
    }

    // Add function to reset DASH state when starting fresh
    fun resetDashState() {
        dashSessionKey = null
        dashResult = null
        clearDashTelemetry()
    }

    // Update resetUploadState to also reset DASH state if needed
    fun resetAllStates() {
        resetDashState()
        resetUploadState()
    }


    private fun copyThumbnailTo(outDir: File, thumb: String, context: Context): File {
        val uri = runCatching { thumb.toUri() }.getOrNull()
        val dest = File(outDir, "thumbnail.png")

        return if (uri != null && (uri.scheme == "content" || uri.scheme == "file")) {
            // Content/File URI -> stream copy
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            dest
        } else {
            // Raw path string -> file copy
            val src = File(thumb)
            src.copyTo(dest, overwrite = true)
        }
    }

    // Optional helpers for UI rendering
    fun dashElapsedText(): String {
        val seconds = (dashElapsedMs.coerceAtLeast(0)) / 1000.0
        return String.format(Locale.US, "%.2f s", seconds)
    }
    fun dashEtaText(): String = formatTime((dashEtaMs / 1000).coerceAtLeast(0))
    // --- END LIVE DASH STATE ---


    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIdx >= 0 && c.moveToFirst()) c.getString(nameIdx) else null
        }
    }

    private fun copyContentUriToFile(context: Context, uri: Uri, dest: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            true
        } catch (_: Exception) {
            false
        }
    }


    /**
     * Uploading to the server
     *
     */

    var isUploading by mutableStateOf(false)
    var uploadTotalBytes by mutableLongStateOf(0L)
    var uploadUploadedBytes by mutableLongStateOf(0L)
    var uploadPercentage by mutableFloatStateOf(0f) // 0.0..100.0
    var uploadSpeedBytesPerSec by mutableLongStateOf(0L)
    var uploadEtaSeconds by mutableLongStateOf(0L)

    private fun resetUploadTelemetry() {
        isUploading = false
        uploadTotalBytes = 0L
        uploadUploadedBytes = 0L
        uploadPercentage = 0f
        uploadSpeedBytesPerSec = 0L
        uploadEtaSeconds = 0L
    }

    private fun updateUploadTelemetry(total: Long, uploaded: Long, startedNs: Long) {
        val elapsedSec = ((System.nanoTime() - startedNs) / 1_000_000_000.0).coerceAtLeast(0.001)
        val speed = (uploaded / elapsedSec).toLong() // bytes/sec (average)
        val remainingBytes = (total - uploaded).coerceAtLeast(0)
        val etaSec = if (speed > 0) (remainingBytes / speed) else 0L
        val pct = if (total > 0) ((uploaded.toDouble() / total.toDouble()) * 100.0).toFloat() else 0f

        // Update state on main thread for future UI
        screenModelScope.launch(Dispatchers.Main) {
            uploadTotalBytes = total
            uploadUploadedBytes = uploaded
            uploadPercentage = pct
            uploadSpeedBytesPerSec = speed
            uploadEtaSeconds = etaSec
        }

        // Also log coarse progress
        println("[Upload] Progress: $uploaded/$total bytes • ${pct.toInt()}% • speed=${formatTransferSpeed(speed)} • eta=${formatTime(etaSec)}")
    }

    // 2) Replace your uploadDashVideo with this version (logic unchanged, telemetry added)
    private var uploadSessionKey by mutableStateOf<String?>(null)
    var uploadResult by mutableStateOf<Result<String>?>(null)
        private set

    // Update the uploadDashVideo function to check for existing session
    suspend fun uploadDashVideo(
        context: Context,
        dashBuildResult: DashBuildResult,
        apiBase: String = MwiUtils.BASE_URL,
        batchSize: Int = 5,
        onDone: () -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        val currentSessionKey = dashBuildResult.outputDir.absolutePath

        // Return existing result if upload already completed for this session
        uploadResult?.let { result ->
            if (uploadSessionKey == currentSessionKey) {
                println("[Upload] Session already completed: $currentSessionKey")
                return@withContext result
            }
        }

        // Prevent multiple uploads for the same session
        if (isUploading && uploadSessionKey == currentSessionKey) {
            println("[Upload] Upload already in progress for session: $currentSessionKey")
            return@withContext Result.failure(IllegalStateException("Upload already in progress"))
        }

        // Set session key to track this upload
        uploadSessionKey = currentSessionKey

        try {
            val form = CreateVideoForm(
                uId = getDeviceId(context),
                title = title.reduceSpaces(),
                description = description.reduceSpaces(),
                nsfw = nsfw,
                duration = dashBuildResult.durationMs,
                manifest = dashBuildResult.manifest,
                thumbnail = dashBuildResult.thumbnail
            )

            val segments = dashBuildResult.segments
            val totalBytes = form.manifest.length() + form.thumbnail.length() + segments.sumOf { it.length() }
            var uploadedBytes = 0L
            val startedNs = System.nanoTime()

            screenModelScope.launch(Dispatchers.Main) {
                isUploading = true
                uploadTotalBytes = totalBytes
                uploadUploadedBytes = 0L
                uploadPercentage = 0f
                uploadSpeedBytesPerSec = 0L
                uploadEtaSeconds = 0L
            }

            println("[Upload] Create: start")
            val videoId = httpClient.post("$apiBase/api/videos/create") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("uId", form.uId)
                            append("title", form.title)
                            append("description", form.description)
                            append("nsfw", form.nsfw.toString())
                            append("duration", form.duration.toString())

                            append(
                                "playlist",
                                InputProvider { form.manifest.inputStream().asInput() },
                                Headers.build {
                                    append(HttpHeaders.ContentType, "application/dash+xml")
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"${form.manifest.name}\""
                                    )
                                }
                            )

                            append(
                                "thumbnail",
                                InputProvider { form.thumbnail.inputStream().asInput() },
                                Headers.build {
                                    append(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"${form.thumbnail.name}\""
                                    )
                                }
                            )
                        }
                    )
                )
            }.body<String>().also {
                uploadedBytes += form.manifest.length() + form.thumbnail.length()
                println("[Upload] Create: success (videoId=$it)")
                updateUploadTelemetry(totalBytes, uploadedBytes, startedNs)
            }

            if (videoId.isBlank()) {
                val result = Result.failure<String>(IllegalStateException("Empty videoId"))
                uploadResult = result
                return@withContext result
            }

            val batches = if (batchSize <= 0) listOf(segments) else segments.chunked(batchSize)
            val totalFiles = segments.size
            var uploadedFiles = 0
            println("[Upload] Segments: totalFiles=$totalFiles, batches=${batches.size}")

            batches.forEachIndexed { idx, batch ->
                val batchBytes = batch.sumOf { it.length() }
                println("[Upload] Batch ${idx + 1}/${batches.size}: start • batchBytes=$batchBytes")

                val batchStartNs = System.nanoTime()
                httpClient.post("$apiBase/api/videos/$videoId/segments") {
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                batch.forEach { seg ->
                                    append(
                                        "files",
                                        InputProvider { seg.inputStream().asInput() },
                                        Headers.build {
                                            append(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                                            append(
                                                HttpHeaders.ContentDisposition,
                                                "filename=\"${seg.name}\""
                                            )
                                        }
                                    )
                                }
                            }
                        )
                    )
                }

                uploadedFiles += batch.size
                uploadedBytes += batchBytes

                val batchElapsedSec = ((System.nanoTime() - batchStartNs) / 1_000_000_000.0).coerceAtLeast(0.001)
                val batchSpeed = (batchBytes / batchElapsedSec).toLong()
                println("[Upload] Batch ${idx + 1}/${batches.size}: success • files=$uploadedFiles/$totalFiles • batchSpeed=${formatTransferSpeed(batchSpeed)}")

                updateUploadTelemetry(totalBytes, uploadedBytes, startedNs)
            }

            println("[Upload] Finalize: start")
            httpClient.post("$apiBase/api/videos/$videoId/finalize")
            println("[Upload] Finalize: success")
            updateUploadTelemetry(totalBytes, totalBytes, startedNs)

            val result = Result.success(videoId)
            uploadResult = result
            result
        } catch (e: Exception) {
            println("[Upload] Failed: ${e.message}")
            e.printStackTrace()
            val result = Result.failure<String>(e)
            uploadResult = result
            result
        } finally {
            screenModelScope.launch(Dispatchers.Main) {
                isUploading = false
                onDone()
            }
        }
    }

    // Add function to reset upload state when starting fresh
    fun resetUploadState() {
        uploadSessionKey = null
        uploadResult = null
        resetUploadTelemetry()
    }

    // delete cache
    fun clearUploadCache(context: Context) {
        val cacheDir = uploadCacheDir(context)
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
            println("Upload cache cleared: ${cacheDir.absolutePath}")
        } else {
            println("Upload cache directory does not exist: ${cacheDir.absolutePath}")
        }
    }

    fun getData(context: Context, dashBuildResult: DashBuildResult): String {
        val createVideoForm = CreateVideoForm(
            uId = getDeviceId(context),
            title = title.reduceSpaces(),
            description = description.reduceSpaces(),
            nsfw = nsfw,
            duration = dashBuildResult.durationMs,
            manifest = dashBuildResult.manifest,
            thumbnail = dashBuildResult.thumbnail,
        )
        return "uId: ${createVideoForm.uId}\n" +
                "title: ${createVideoForm.title}\n" +
                "description: ${createVideoForm.description}\n" +
                "duration: ${createVideoForm.duration} ms\n" +
                "manifest: ${createVideoForm.manifest.absolutePath}\n" +
                "thumbnail: ${createVideoForm.thumbnail.absolutePath}"
    }

    /**
     * Video Conversion to DASH
     */

//    suspend fun convertVideoToDash(context: Context, inputFile: File): File? =
//        withContext(Dispatchers.IO) {
//            val outputDir = File(
//                uploadCacheDir(context),
//                "dash_${inputFile.nameWithoutExtension}_${System.currentTimeMillis()}"
//            )
//            if (!outputDir.exists()) outputDir.mkdirs()
//            val outputMpd = File(outputDir, "index.mpd")
//
//            val ffmpegCommand = arrayOf(
//                "-i", inputFile.absolutePath,
//                "-c", "copy",
//                "-f", "dash",
//                "-seg_duration", "4",
//                "-use_template", "1",
//                "-use_timeline", "1",
//                outputMpd.absolutePath
//            )
//
//            val session = FFmpegKit.execute(ffmpegCommand.joinToString(" "))
//            val returnCode = session.returnCode
//
//            if (ReturnCode.isSuccess(returnCode)) {
//                outputMpd
//            } else {
//                outputDir.deleteRecursively()
//                null
//            }
//        }

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
//    suspend fun onUploadDashButtonClicked(
//        allFiles: List<File>,
//        title: String,
//        description: String
//    ) {
//        // This function now runs on the thread of its caller.
//        // The 'uploadDashVideoInBatches' function should be called from a coroutine.
//        // Example: scope.launch(Dispatchers.IO) { onUploadDashButtonClicked(...) }
//
//        // IMPROVEMENT: Find the manifest, then treat everything else as a segment.
//        // This avoids hardcoding the ".m4s" extension and is more robust.
//        val manifestFile = allFiles.firstOrNull { it.extension == "mpd" }
//        val segmentFiles = allFiles.filter { it != manifestFile }
//
//        if (manifestFile == null || segmentFiles.isEmpty()) {
//            // IMPROVEMENT: More generic error message.
//            println("Error: Manifest (.mpd) or segment files not found in the list.")
//            // Show error to user
//            return
//        }
//
//        // Call the DASH upload function, which remains unchanged.
//        // Make sure this is called from a coroutine scope, e.g., scope.launch { ... }
//        val result = uploadDashVideoInBatches(manifestFile, segmentFiles, title, description)
//
//        // Handle the result on the main thread if updating UI
//        result.onSuccess { successMessage ->
//            println(successMessage)
//            // e.g., withContext(Dispatchers.Main) { show success UI }
//        }.onFailure { exception ->
//            println("Upload failed: ${exception.message}")
//            // e.g., withContext(Dispatchers.Main) { show error UI }
//        }
//    }


    /**
     * The core upload function.
     * NO CHANGES ARE NEEDED HERE. It correctly accepts the separated manifest and segment files.
     */
//    suspend fun uploadDashVideoInBatches(
//        manifestFile: File,
//        segmentFiles: List<File>,
//        title: String,
//        description: String
//    ): Result<String> {
//        // ... same implementation as before
//        val url = "${Resources.BASE_URL}/api/videos"
//
//        try {
//            // STEP 1: Create video record and upload manifest
//            println("Step 1: Creating video record and uploading DASH manifest...")
//            val videoId = httpClient.post("$url/create") {
//                setBody(
//                    MultiPartFormDataContent(
//                        formData {
//                            append("title", title)
//                            append("description", description)
//                            append("playlist", manifestFile.readBytes(), Headers.build {
//                                append(HttpHeaders.ContentType, "application/dash+xml")
//                                append(
//                                    HttpHeaders.ContentDisposition,
//                                    "filename=\"${manifestFile.name}\""
//                                )
//                            })
//                        }
//                    ))
//            }.body<String>()
//
//            if (videoId.isBlank()) {
//                return Result.failure(Exception("Failed to create video record: Server returned an empty ID."))
//            }
//            println("Step 1 SUCCESS. Received videoId: $videoId")
//
//
//            // STEP 2: Upload segments using streaming to save memory
//            println("Step 2: Uploading segments in batches...")
//            val batchSize = 25
//            val segmentBatches = segmentFiles.chunked(batchSize)
//
//            segmentBatches.forEachIndexed { index, batch ->
//                println("Uploading batch ${index + 1} of ${segmentBatches.size}...")
//                httpClient.post("$url/$videoId/segments") {
//                    setBody(
//                        MultiPartFormDataContent(
//                            formData {
//                                batch.forEach { segmentFile ->
//                                    append(
//                                        "files",
//                                        InputProvider { segmentFile.inputStream().asInput() },
//                                        Headers.build {
//                                            append(HttpHeaders.ContentType, "video/mp4")
//                                            append(
//                                                HttpHeaders.ContentDisposition,
//                                                "filename=\"${segmentFile.name}\""
//                                            )
//                                        })
//                                }
//                            }
//                        ))
//                }
//            }
//            println("Step 2 SUCCESS. All segment batches uploaded.")
//
//
//            // STEP 3: Finalize (This step is usually format-agnostic and needs no changes)
//            println("Step 3: Finalizing upload...")
//            httpClient.post("$url/$videoId/finalize")
//            println("Step 3 SUCCESS. Video is marked as ready.")
//
//            return Result.success("Upload complete! Video ID: $videoId")
//
//        } catch (e: Exception) {
//            println("Upload failed during the process: ${e.message}")
//            e.printStackTrace()
//            return Result.failure(e)
//        }
//    }

//    suspend fun extractFrameFromExoPlayer(
//        context: Context,
//        exoPlayer: ExoPlayer,
//        timeInMillis: Long = extractFrameAt
//    ): String? {
//        return try {
//            // Use the passed timeInMillis instead of current position
//            withContext(Dispatchers.IO) {
//                val outputFile =
//                    File(uploadCacheDir(context), "extracted_frame_${System.currentTimeMillis()}.png")
//
//                // Use MediaMetadataRetriever to extract the frame
//                val retriever = MediaMetadataRetriever()
//                val timeUs = timeInMillis * 1000 // Use the saved position
//
//                // Copy the video file to a temporary location for MediaMetadataRetriever
//                val tempFile = File.createTempFile("temp_video_", ".mp4", context.cacheDir)
//                val inputStream = context.contentResolver.openInputStream(fileUri.toUri())
//
//                inputStream?.use { input ->
//                    tempFile.outputStream().use { output ->
//                        input.copyTo(output)
//                    }
//                }
//
//                retriever.setDataSource(tempFile.absolutePath)
//                val bitmap =
//                    retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
//
//                if (bitmap != null) {
//                    // Save the bitmap to a file
//                    outputFile.outputStream().use { out ->
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
//                    }
//                    retriever.release()
//                    tempFile.delete()
//                    outputFile.absolutePath
//                } else {
//                    retriever.release()
//                    tempFile.delete()
//                    null
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }

// Extras (Utilities)
    fun getFileAtCacheFromUri(context: Context, uri: Uri): File? {
        // Already a file path
        if (ContentResolver.SCHEME_FILE == uri.scheme) {
            return uri.path?.let(::File)
        }

        // Content Uri that points to a file we already cached
        val cr = context.contentResolver
        val name =
            cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            } ?: uri.lastPathSegment

        if (name != null) {
            val uploadCache = File(context.cacheDir, "UploadCache")
            val inUploadCache = File(uploadCache, name)
            if (inUploadCache.exists()) return inUploadCache

            val inCache = File(context.cacheDir, name)
            if (inCache.exists()) return inCache
        }

        return null
    }
}