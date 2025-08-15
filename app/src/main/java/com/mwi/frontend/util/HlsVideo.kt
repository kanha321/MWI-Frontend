package com.mwi.frontend.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

var statusText by mutableStateOf("")
suspend fun convertVideoToHls(context: Context, inputFile: File): File? =
    withContext(Dispatchers.IO) {
        val outputDir = File(context.cacheDir, "hls_${System.currentTimeMillis()}")
        if (!outputDir.exists()) outputDir.mkdirs()
        val outputM3u8 = File(outputDir, "index.m3u8")

        val ffmpegCommand = arrayOf(
            "-i", inputFile.absolutePath,
            "-c:v", "copy",
            "-c:a", "copy",
            "-start_number", "0",
            "-hls_time", "5",
            "-hls_list_size", "0",
            "-f", "hls",
            outputM3u8.absolutePath
        )

        val session = FFmpegKit.execute(ffmpegCommand.joinToString(" "))
        val returnCode = session.returnCode

        if (ReturnCode.isSuccess(returnCode)) {
            outputM3u8
        } else {
            null
        }
    }

suspend fun createHlsFromUri(context: Context, videoUri: Uri): List<File>? {
    val cachedFile = copyUriToCacheFile(context, videoUri, "temp_video_file.mp4") ?: return null
    val m3u8File = convertVideoToHls(context, cachedFile) ?: return null
    val outputDir = m3u8File.parentFile ?: return null
    val hlsFiles = outputDir.listFiles { file ->
        file.extension == "ts" || file.extension == "m3u8"
    }?.toList() ?: return null

    return hlsFiles
}

fun copyUriToCacheFile(context: Context, uri: Uri, filename: String): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File(context.cacheDir, filename)
        val outputStream = FileOutputStream(tempFile)

        statusText = "Copying Video to Cache"

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

suspend fun convertVideoToDash(context: Context, inputFile: File): File? =
    withContext(Dispatchers.IO) {
        val outputDir = File(context.cacheDir, "dash_${inputFile.nameWithoutExtension}_${System.currentTimeMillis()}")
        if (!outputDir.exists()) outputDir.mkdirs()
        val outputMpd = File(outputDir, "index.mpd")

        statusText = "Converting Video to Dash"
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

/**
 * Main orchestrator function. Copies a video from a Uri, converts it to DASH,
 * and extracts a thumbnail, placing all generated files in a single output directory.
 *
 * @param context The application context.
 * @param videoUri The Uri of the video to process.
 * @param durationMsForThumbnail The timestamp in milliseconds for thumbnail extraction.
 * @return A List of all generated files (DASH segments, MPD, thumbnail) on success, or null on failure.
 */
suspend fun createDashFromUri(
    context: Context,
    videoUri: Uri,
    durationMsForThumbnail: Long
): List<File>? {
    // Create a temporary file to hold the raw video input
    val cachedFile = File(context.cacheDir, "temp_input_${System.currentTimeMillis()}.mp4")

    try {
        // 1. Copy the video from the Uri to a local cache file
        if (copyUriToCacheFile(context, videoUri, cachedFile.name) == null) {
            return null
        }

        // 2. Convert the cached video file to DASH format
        val mpdFile = convertVideoToDash(context, cachedFile) ?: return null

        // 3. Get the output directory created by the DASH conversion
        val outputDir = mpdFile.parentFile ?: return null

        // 4. Extract the thumbnail and place it in the SAME directory
        val thumbnailFile = extractThumbnailFromVideo(
            inputFile = cachedFile,
            outputDir = outputDir,
            durationMs = durationMsForThumbnail
        )

        if (thumbnailFile == null) {
            Log.e("Workflow", "Thumbnail extraction failed, but DASH conversion succeeded.")
        }

        // 5. Return a list of all files in the directory
        return outputDir.listFiles()?.toList()
    } finally {
        // 6. Clean up the temporary input file in all cases
        if (cachedFile.exists()) {
            cachedFile.delete()
        }
    }
}

/**
 * Extracts a single frame from a video file and saves it as a JPEG thumbnail
 * inside a specific directory.
 *
 * @param inputFile The video file from which to extract the thumbnail.
 * @param outputDir The target directory where "thumbnail.jpg" will be saved.
 * @param durationMs The timestamp in milliseconds at which to capture the frame.
 * @return The [File] object for the created "thumbnail.jpg" on success, or null on failure.
 */
@SuppressLint("DefaultLocale")
suspend fun extractThumbnailFromVideo(
    inputFile: File,
    outputDir: File,
    durationMs: Long
): File? = withContext(Dispatchers.IO) {
    // This function no longer creates its own directory.
    // It saves the thumbnail in the provided outputDir.
    val outputJpg = File(outputDir, "thumbnail.jpg")

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