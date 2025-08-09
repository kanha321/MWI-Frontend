package com.mwi.frontend

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.kanhaji.basics.AppContent
import com.kanhaji.basics.composables.MySnackBarObject
import com.kanhaji.basics.composables.MySnackbarHost
import com.kanhaji.basics.legacy.TestContent
import com.kanhaji.basics.theme.AppTheme
import com.kanhaji.basics.theme.BasicKolorTheme
import com.kanhaji.basics.theme.InitTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

var isDark by mutableStateOf(false)

@Composable
@Preview
fun App() {

    BasicKolorTheme {
        InitTheme()
        AppContent("MWI")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppComponent() {

    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    MySnackBarObject.snackbarHostState = snackbarHostState


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My App") },
                actions = {
                    IconButton(onClick = { /* Handle action */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        snackbarHost = { MySnackbarHost() }
    ) {
        AppContent("MWI")
    }
}