package com.kanhaji.basics.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicFABTemplate() {
    val scope = rememberCoroutineScope()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Material 3 Expressive") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                showSnackbar("Settings clicked")
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                        )
                    }
                }
            )
        },
        snackbarHost = {
//            SnackbarHost(
//                hostState = snackbarHostState,
//                snackbar = { data ->
//                    CustomSnackbar(data)
//                }
//            )
            MySnackbarHost()
        },
        floatingActionButton = {
            DynamicFab(fabVisible)
        },
        modifier = Modifier.fillMaxSize(),
    ) { contentPadding ->
        GenericLazyColumn(
            items = List(100) { index -> "Item ${index + 1}" },
            contentPadding = contentPadding,
            listState = listState
        ) { index, item ->
            Text(
                text = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(56.dp)
                    .wrapContentHeight()
            )
        }
//        Spacer(Modifier.padding(contentPadding))
//        CardColumnGroup(
//            items = List(100) { "Item ${it + 1}" },
//            state = listState
//        )
    }
}

@Composable
fun DynamicFab(visibility: Boolean) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Show text when originally visible OR when hovered and originally hidden
    val shouldShowText = visibility || (!visibility && isHovered)

    FloatingActionButton(
        onClick = {
            scope.launch {
                showSnackbar("Snackbar") { data ->
                    CustomSnackbarYesNo(data)
                }
            }
        },
        interactionSource = interactionSource,
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "",
                modifier = Modifier.padding(8.dp)
            )
            AnimatedVisibility(
                visible = shouldShowText,
            ) {
                Spacer(Modifier.width(12.dp))
                Text("Action", modifier = Modifier.padding(start = 12.dp))
            }
        }
    }
}