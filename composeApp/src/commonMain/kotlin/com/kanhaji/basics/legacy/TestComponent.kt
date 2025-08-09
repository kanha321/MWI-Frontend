package com.kanhaji.basics.legacy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kanhaji.basics.Greeting
import com.kanhaji.basics.composables.KButton
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.networking.httpClient
import com.kanhaji.basics.theme.BasicKolorTheme
import com.kanhaji.basics.theme.ThemeManager
import com.kanhaji.basics.theme.getSystemPrimaryColor
import com.kanhaji.basics.theme.isSystemDarkTheme
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun TestContent() {
    var showContent by remember { mutableStateOf(false) }
    var scope = rememberCoroutineScope()
    var bText by remember { mutableStateOf("Button") }
    BasicKolorTheme {
        Scaffold {
            Column(
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = { showContent = !showContent }) {
                    Text("Click me!")
                }
                AnimatedVisibility(showContent) {
                    val greeting = remember { Greeting().greet() }
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(imageVector = Icons.Default.Build, null)
                        Text("Compose: $greeting")
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth().background(getSystemPrimaryColor()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SelectionContainer {
                        Text(getSystemPrimaryColor().toString())
                    }
                }
                SelectionContainer {
                    Text("ThemeManager.isSystemDark = " + ThemeManager.isSystemDark)
                }
                Button(
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = 0.38f
                        ),
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.12f
                        )
                    ),
                    onClick = {
                        scope.launch {
                            ThemeManager.isDarkTheme = !ThemeManager.isDarkTheme
                            PrefsManager.saveBoolean("darkTheme", ThemeManager.isDarkTheme)
                        }
                    }
                ) {
                    Text("ThemeManager.isDarkTheme = " + ThemeManager.isDarkTheme)
                }

                KButton(onClick = {
                    scope.launch {
                        try {
                            val response =
                                httpClient.get("https://jsonplaceholder.typicode.com/todos/1") {
                                    contentType(ContentType.Application.Json)
                                }
                            bText = response.bodyAsText()
                        } catch (e: Exception) {
                            bText = "Error: ${e.message ?: "Unknown error"}"
                        }
                    }
                }) {
                    Text(bText)
                }
            }
        }
    }
}