package com.mwi.frontend.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.basics.extensions.toTitleCase
import com.kanhaji.basics.networking.httpClient
import com.kanhaji.basics.screens.settings.SettingsScreen
import com.mwi.frontend.screens.player.PlayerScreen
import com.mwi.frontend.util.FileType
import com.mwi.frontend.util.Resources
import com.mwi.frontend.util.createDashFromUri
import com.mwi.frontend.util.openFilePicker
import io.ktor.client.call.body
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeComponentTest() {

    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()

    var file by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var hls by remember { mutableStateOf(emptyList<File>()) }
    var statusText by remember { mutableStateOf("Ready") }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MWI") },
                actions = {
                    IconButton(
                        onClick = {
                            navigator.push(SettingsScreen)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    IconButton(
                        onClick = {
                            navigator.push(PlayerScreen)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "PlayerScreen"
                        )
                    }
                },
                navigationIcon = {
                    if (navigator.canPop)
                        IconButton(
                            onClick = {
                                navigator.pop()
                            },
                            content = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var test by remember { mutableStateOf("Test Connection") }
            Button(onClick = {
                scope.launch {
                    test = try {
                        httpClient.get("${Resources.BASE_URL}/health-check").bodyAsText()
                    } catch (e: Exception) {
                        "Error: ${e.message ?: "Unknown error"}"
                    }
                }
            }) {
                Text(test)
            }
            Text(
                text = test,
                modifier = Modifier.padding(16.dp)
            )
            SelectionContainer {
                Text(
                    text = file.toString(),
                )
            }
            Text(
                text = "Status: $statusText"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                scope.launch {
                    file = openFilePicker(context, FileType.VIDEO)
                }
            }) {
                Text("Load a video file")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        if (file != null) {
                            statusText = "Creating HLS video from: ${file!!.toTitleCase()}"
                            val hlsFiles = createDashFromUri(context, file!!.toUri(), 10000L)
                            if (hlsFiles != null) {
                                hls = hlsFiles
                                statusText =
                                    "HLS video created successfully. Files: ${hlsFiles.size}"
                            } else {
                                statusText = "Failed to create HLS video."
                            }
                        } else {
                            statusText = "Please load a video file first."
                        }
                    }
                }
            ) {
                Text("Create HLS Video")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                scope.launch {
                    if (hls.isNotEmpty()) {
                        statusText = "Video upload initiated."
                        statusText = onUploadDashButtonClicked(
                            allFiles = hls,
                            title = "HLS Video",
                            description = "Uploaded from MWI app"
                        ).toString()
                    } else {
                        statusText = "Please create HLS video first."
                    }
                }
            }) {
                Text("Upload HLS Video")
            }
        }
    }
}

suspend fun uploadVideo(files: List<File>, title: String, description: String): Result<String> {
    // IMPORTANT: Make sure this URL is correct. Use 10.0.2.2 for emulator -> localhost.
    val uploadUrl = "${Resources.BASE_URL}/api/videos/upload"

    return try {
        // Here we use your 'httpClient' and build the request correctly
        val response = httpClient.post(uploadUrl) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("title", title)
                        append("description", description)

                        files.forEach { file ->
                            append("files", file.readBytes(), Headers.build {
                                val contentType = when (file.extension.lowercase()) {
                                    "m3u8" -> "application/vnd.apple.mpegurl"
                                    "ts" -> "video/mp2t"
                                    else -> "application/octet-stream"
                                }
                                append(HttpHeaders.ContentType, contentType)
                                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                            })
                        }
                    }
                ))
        }

        if (response.status.isSuccess()) {
            Result.success("Upload successful! Server responded with: ${response.body<String>()}")
        } else {
            val errorBody = response.body<String>()
            val errorMsg = "Upload failed: ${response.status.value} - $errorBody"
            Result.failure(Exception(errorMsg))
        }
    } catch (e: Exception) {
        // This will catch network errors, timeouts, etc.
        Result.failure(e)
    }
}


// In your ViewModel or wherever you trigger the upload

fun onUploadButtonClicked(allFiles: List<File>, title: String, description: String) {
    GlobalScope.launch {
        // Separate the playlist from the segments
        val playlistFile = allFiles.firstOrNull { it.extension == "m3u8" }
        val segmentFiles = allFiles.filter { it.extension == "ts" }

        if (playlistFile == null || segmentFiles.isEmpty()) {
            println("Error: M3U8 playlist or TS segments not found.")
            // Show error to user
            return@launch
        }

        // Call the NEW function
        val result = uploadVideoInBatches(playlistFile, segmentFiles, title, description)

        result.onSuccess { successMessage ->
            // Show success to user
            println(successMessage)
        }.onFailure { exception ->
            // Show error to user
            println("Upload failed: ${exception.message}")
        }
    }
}

/**
 * The NEW upload function that uses the robust, multi-step API.
 *
 * @param playlistFile The single .m3u8 file.
 * @param segmentFiles The list of all .ts segment files.
 * @param title The video title.
 * @param description The video description.
 */

suspend fun uploadVideoInBatches(
    playlistFile: File,
    segmentFiles: List<File>,
    title: String,
    description: String
): Result<String> {

    val baseUrl = "${Resources.BASE_URL}/api/videos"

    try {
        // STEP 1: Create video record (This part is small, readBytes() is fine here)
        println("Step 1: Creating video record and uploading manifest...")
        val videoId = httpClient.post("$baseUrl/create") {
            setBody(
                MultiPartFormDataContent(
                formData {
                    append("title", title)
                    append("description", description)
                    append("playlist", playlistFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "application/vnd.apple.mpegurl")
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=\"${playlistFile.name}\""
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
            httpClient.post("$baseUrl/$videoId/segments") {
                setBody(
                    MultiPartFormDataContent(
                    formData {
                        batch.forEach { segmentFile ->
                            // THIS IS THE KEY CHANGE:
                            // We no longer use .readBytes(). We provide an input stream.
                            // This uses almost no memory, regardless of file size.
                            append(
                                "files",
                                InputProvider { segmentFile.inputStream().asInput() },
                                Headers.build {
                                    append(HttpHeaders.ContentType, "video/mp2t")
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"${segmentFile.name}\""
                                    )
                                    // Ktor will automatically set the Content-Length
                                })
                        }
                    }
                ))
            }
        }
        println("Step 2 SUCCESS. All segment batches uploaded.")


        // STEP 3: Finalize
        println("Step 3: Finalizing upload...")
        httpClient.post("$baseUrl/$videoId/finalize")
        println("Step 3 SUCCESS. Video is marked as ready.")

        return Result.success("Upload complete! Video ID: $videoId")

    } catch (e: Exception) {
        println("Upload failed during the process: ${e.message}")
        e.printStackTrace()
        return Result.failure(e)
    }
}

// Assume 'httpClient' is an instance of io.ktor.client.HttpClient you have configured

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
    val thumbnailFile = allFiles.firstOrNull { it.extension == "jpg" }
    val segmentFiles = allFiles.filter { it != manifestFile }

    if (manifestFile == null || thumbnailFile == null || segmentFiles.isEmpty()) {
        // IMPROVEMENT: More generic error message.
        println("Error: Manifest (.mpd), thumbnail or segment files not found in the list.")
        // Show error to user
        return
    }

    // Call the DASH upload function, which remains unchanged.
    // Make sure this is called from a coroutine scope, e.g., scope.launch { ... }
    val result = uploadDashVideoInBatches(manifestFile, thumbnailFile, segmentFiles, title, description)

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
    thumbnailFile: File,
    segmentFiles: List<File>,
    title: String,
    description: String
): Result<String> {
    // ... same implementation as before
    val baseUrl = "${Resources.BASE_URL}/api/videos"

    try {
        // STEP 1: Create video record and upload manifest
        println("Step 1: Creating video record and uploading DASH manifest...")
        val videoId = httpClient.post("$baseUrl/create") {
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
            httpClient.post("$baseUrl/$videoId/segments") {
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
        httpClient.post("$baseUrl/$videoId/finalize")
        println("Step 3 SUCCESS. Video is marked as ready.")

        return Result.success("Upload complete! Video ID: $videoId")

    } catch (e: Exception) {
        println("Upload failed during the process: ${e.message}")
        e.printStackTrace()
        return Result.failure(e)
    }
}