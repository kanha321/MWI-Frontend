package com.mwi.frontend.screens.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.saveable.rememberSaveable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.basics.composables.KButton
import com.kanhaji.basics.networking.httpClient
import com.kanhaji.basics.screens.settings.SettingsScreen
import com.mwi.frontend.ui.components.ExoPlayerComponent
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerComponentTest() {
    var videoUrl by rememberSaveable { mutableStateOf("") }
    var showPlayer by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()
    var test by remember { mutableStateOf("Test Connection") }
    val url = "http://10.87.148.86:8080/health-check"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Player") },
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
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectionContainer {
                Text(
                    text = url,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            KButton(onClick = {
                scope.launch {
                    test = try {
                        httpClient.get(url).bodyAsText()
                    } catch (e: Exception) {
                        "Error: ${e.message ?: "Unknown error"}"
                    }
                }
            }) {
                Text(test)
            }
            OutlinedTextField(
                value = videoUrl,
                onValueChange = {
                    videoUrl = it
                    showPlayer = false // Hide player when URL changes
                },
                label = { Text("Video URL") },
                placeholder = { Text("Enter video URL here...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            KButton(
                onClick = {
                    if (videoUrl.isNotBlank()) {
                        showPlayer = true
                    } else {
                        Toast.makeText(context, "Please enter a video URL", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Play Video")
            }

            if (showPlayer && videoUrl.isNotBlank()) {
                ExoPlayerComponent(videoUrl = videoUrl)
            }
        }
    }
}
