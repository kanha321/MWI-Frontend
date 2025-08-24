package com.mwi.frontend.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.basics.composables.DynamicFab
import com.kanhaji.basics.composables.MySnackBarObject
import com.mwi.frontend.entity.VideoMetadata
import com.mwi.frontend.screens.home.components.VideoItem
import com.mwi.frontend.screens.player.PlayerScreen
import com.mwi.frontend.screens.upload.UploadScreen
import com.mwi.frontend.ui.components.KAppBar
import com.mwi.frontend.util.FileType
import com.mwi.frontend.util.MwiUtils
import com.mwi.frontend.util.openFilePicker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeComponent(screenModel: HomeScreenModel) {
    val navigator = LocalNavigator.currentOrThrow
    val context = LocalContext.current

    var videoMetadata by remember { mutableStateOf<List<VideoMetadata>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }


    val scope = rememberCoroutineScope()


    suspend fun refreshVideos() {
        try {
            isLoading = true
            videoMetadata = screenModel.getAllVideos()
            error = null
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }


    val snackbarHostState = remember { SnackbarHostState() }
    MySnackBarObject.snackbarHostState = snackbarHostState

    // Detect scroll direction
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

    LaunchedEffect(Unit) {
        MwiUtils.clearCache(context)
        try {
            isLoading = true
            videoMetadata = screenModel.getAllVideos()
            error = null
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            KAppBar() {
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
            isRefreshing = isLoading,
            state = rememberPullToRefreshState(),
            onRefresh = {
                scope.launch {
                    refreshVideos()
                }
            }
        ) {
            if (isLoading) {
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
            } else if (error != null) {
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
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(innerPadding),
                ) {
                    itemsIndexed(videoMetadata) { index, video ->
                        VideoItem(video) {
                            navigator.push(PlayerScreen(video.toString()))
                        }
                    }
                }
            }
        }
    }
}