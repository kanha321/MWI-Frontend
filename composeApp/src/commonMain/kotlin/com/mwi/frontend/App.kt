package com.mwi.frontend

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.kanhaji.basics.theme.BasicKolorTheme
import com.kanhaji.basics.theme.InitTheme
import com.mwi.frontend.screens.HomeScreen
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

@Composable
fun AppContent(
    pageTitle: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold {
            // Main content area
            Navigator(HomeScreen) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AppContent(
//    pageTitle: String,
//) {
//    val scope = rememberCoroutineScope()
//
//    val snackbarHostState = remember { SnackbarHostState() }
//    MySnackBarObject.snackbarHostState = snackbarHostState
//
//    val listState = rememberLazyListState()
//    ListState.value = listState
//
//    Box {
//        Scaffold(
//
//            topBar = {
//                TopAppBar(
//                    title = { Text(pageTitle) },
//                )
//            },
//            snackbarHost = {
//                MySnackbarHost()
//            },
//            modifier = Modifier.fillMaxSize(),
//        ) { contentPadding ->
//            Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
//                // Main content area
//                Navigator(HomeScreen) { navigator ->
//                    SlideTransition(navigator)
//                }
//            }
////            Navigator(SettingsScreen) { navigator ->
////                SlideTransition(navigator) { screen ->
////                    (screen as? SettingsScreen)?.let {
////                        SettingsComponent(contentPadding)
////                    }
////                }
////            }
//        }
//    }
//}