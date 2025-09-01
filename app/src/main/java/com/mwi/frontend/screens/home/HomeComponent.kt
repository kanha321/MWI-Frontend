package com.mwi.frontend.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.basics.composables.DynamicFab
import com.kanhaji.basics.composables.MySnackBarObject
import com.kanhaji.basics.util.Updater
import com.mwi.frontend.entity.VideoMetadata
import com.mwi.frontend.screens.home.components.VideoItem
import com.mwi.frontend.screens.player.PlayerScreen
import com.mwi.frontend.screens.upload.UploadScreen
import com.mwi.frontend.ui.components.KAppBar
import com.mwi.frontend.util.AppSettingsItems
import com.mwi.frontend.util.FileType
import com.mwi.frontend.util.MwiUtils
import com.mwi.frontend.util.openFilePicker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeComponent(screenModel: HomeScreenModel) {
    val navigator = LocalNavigator.currentOrThrow
    val context = LocalContext.current

// ---- States from ScreenModel ----
    val videos by remember { derivedStateOf { screenModel.videos } }
    val isLoading by remember { derivedStateOf { screenModel.videosIsLoading } }
    val error by remember { derivedStateOf { screenModel.error } }

    val scope = rememberCoroutineScope()

    // ---- Refresh Function ----
    suspend fun refreshVideos() {
        try {
            screenModel.resetVideos(AppSettingsItems.showNsfwContent)
        } catch (e: Exception) {
            screenModel.error = e.message
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    MySnackBarObject.snackbarHostState = snackbarHostState

// ---- Detect scroll direction ----
    val listState = rememberLazyListState()
    var fabVisible by remember { mutableStateOf(true) }
    var lastScrollOffset by remember { mutableIntStateOf(0) }
    val currentOffset = remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset + listState.firstVisibleItemIndex * 1000
        }
    }
    LaunchedEffect(currentOffset.value) {
        if (listState.isScrollInProgress) {
            fabVisible = currentOffset.value < lastScrollOffset
            lastScrollOffset = currentOffset.value
        }
    }

// ---- Initial Load ----
    LaunchedEffect(Unit) {
        try {
            screenModel.resetVideos(AppSettingsItems.showNsfwContent)
            screenModel.error = null
        } catch (e: Exception) {
            screenModel.error = e.message
        }
        if (!Updater.updateChecked) Updater.checkForUpdates()
        MwiUtils.clearCache(context)
    }

// ---- Infinite Scroll Trigger ----
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.collect { lastVisibleItem ->
            val totalItems = videos.size
            if (lastVisibleItem != null && lastVisibleItem >= totalItems - 1 && !isLoading) {
                // Load next page when user is near bottom
                screenModel.loadNextVideosPage(AppSettingsItems.showNsfwContent)
            }
        }
    }


    Scaffold(
        topBar = {
            KAppBar {
                IconButton(
                    onClick = {
                        scope.launch {
                            refreshVideos()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        },
        floatingActionButton = {
            DynamicFab(
                visibility = fabVisible,
                icon = Icons.Default.Upload,
                text = "Upload Video",
                shape = RoundedCornerShape(100)
            ) {
                scope.launch {
                    openFilePicker(context = context, type = FileType.VIDEO) { uri ->
                        if (uri == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("No file selected")
                            }
                            return@openFilePicker
                        }
                        navigator.push(UploadScreen(uri))
                    }
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
            isRefreshing = isLoading, // <- from screenModel
            state = rememberPullToRefreshState(),
            onRefresh = {
                scope.launch {
                    refreshVideos()
                }
            }
        ) {
            when {
                isLoading && videos.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LoadingIndicator(
                            modifier = Modifier.size(200.dp)
                        )
                    }
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: ${error ?: "Unknown error"}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        itemsIndexed(videos) { index, video ->
                            VideoItem(video) {
                                navigator.push(PlayerScreen(video.toString()))
                            }
                        }

                        // Optional: show loading indicator at bottom when fetching next page
                        if (isLoading && videos.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LoadingIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}