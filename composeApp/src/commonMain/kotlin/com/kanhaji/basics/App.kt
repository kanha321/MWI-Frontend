package com.kanhaji.basics
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.derivedStateOf
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import cafe.adriel.voyager.navigator.Navigator
//import cafe.adriel.voyager.transitions.SlideTransition
//import com.kanhaji.basics.composables.DynamicFab
//import com.kanhaji.basics.composables.ListState
//import com.kanhaji.basics.composables.MySnackBarObject
//import com.kanhaji.basics.composables.MySnackbarHost
//import com.kanhaji.basics.composables.showSnackbar
//import com.kanhaji.basics.screens.settings.SettingsComponent
//import com.kanhaji.basics.screens.settings.SettingsScreen
//import com.kanhaji.basics.theme.InitTheme
//import kotlinx.coroutines.launch
//import kotlinx.serialization.json.Json
//import org.jetbrains.compose.ui.tooling.preview.Preview
//
//@Composable
//@Preview
//fun App() {
//    InitTheme()
//    AppContentWithFAB("Material 3")
//    Json.encodeToString("")
////    TestContent()
//}
//
////@Composable
////fun SetStatusBarColor(color: Color, darkIcons: Boolean = true) {
////    val view = LocalView.current
////    val window = (view.context as? Activity)?.window ?: return
////    SideEffect {
////        WindowCompat.setDecorFitsSystemWindows(window, false)
////        window.statusBarColor = color.toArgb()
////        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = darkIcons
////    }
////}
//
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AppContentWithFAB(
//    pageTitle: String,
//) {
//    val scope = rememberCoroutineScope()
//
//    val snackbarHostState = remember { SnackbarHostState() }
//    MySnackBarObject.snackbarHostState = snackbarHostState
//
//    // Detect scroll direction
//    val listState = rememberLazyListState()
//    var fabVisible by remember { mutableStateOf(true) }
//    var lastScrollOffset by remember { mutableIntStateOf(0) }
//    val currentOffset = remember {
//        derivedStateOf {
//            listState.firstVisibleItemScrollOffset + listState.firstVisibleItemIndex * 1000
//        }
//    }
//    LaunchedEffect(currentOffset.value) {
//        if (listState.isScrollInProgress) {
//            fabVisible = currentOffset.value < lastScrollOffset
//            lastScrollOffset = currentOffset.value
//        }
//    }
//    ListState.value = listState
//
//
//    Box {
//        Scaffold(
////            contentWindowInsets = WindowInsets.safeContent,
//
//            topBar = {
//                TopAppBar(
//                    title = { Text(pageTitle) },
////                    colors = TopAppBarDefaults.topAppBarColors(
////                        containerColor = MaterialTheme.colorScheme.primaryContainer,
////                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
////                    ),
//                    actions = {
//                        IconButton(
//                            onClick = {
//                                scope.launch {
//                                    showSnackbar("Settings clicked")
//                                }
//                            },
//                        ) {
//                            Icon(
//                                imageVector = Icons.Filled.Settings,
//                                contentDescription = "Settings",
//                            )
//                        }
//                    }
//                )
//            },
//            snackbarHost = {
//                MySnackbarHost()
//            },
//            floatingActionButton = {
//                DynamicFab(fabVisible)
//            },
//            modifier = Modifier.fillMaxSize(),
//        ) { contentPadding ->
//            Navigator(SettingsScreen()) { navigator ->
//                SlideTransition(navigator) { screen ->
//                    // Pass contentPadding to the composable
//                    (screen as? SettingsScreen)?.let {
//                        SettingsComponent(contentPadding)
//                    }
//                }
//            }
//        }
//    }
//}