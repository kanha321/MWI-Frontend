package com.mwi.frontend.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kanhaji.basics.composables.DynamicFABTemplate
import com.kanhaji.basics.composables.KButton
import com.mwi.frontend.platforms.openFilePicker
import kotlinx.coroutines.launch

@Composable
fun HomeComponent(

) {
    val scope = rememberCoroutineScope()
    DynamicFABTemplate("MWI") { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var file by remember { mutableStateOf<String?>(null) }
            SelectionContainer {
                Text(
                    text = "Selected File: ${file ?: "None"}",
                    modifier = Modifier.padding(16.dp)
                )
            }
            KButton(
                onClick = {
                    scope.launch {
                        file = openFilePicker()
                    }
                }
            ) {
                Text("Upload Video")
            }
        }
    }
}