package com.mwi.frontend.screens.upload.components.state3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kanhaji.basics.composables.KSwitch
import com.mwi.frontend.screens.upload.UploadScreenModel
import kotlin.math.min
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoComponent(
    screenModel: UploadScreenModel,
    modifier: Modifier = Modifier,
    maxTitleLen: Int = 80,
    maxDescriptionLen: Int = 500
) {
    val titleLen = min(screenModel.title.length, maxTitleLen)
    val descLen = min(screenModel.description.length, maxDescriptionLen)

    val focusManager = LocalFocusManager.current
    val titleFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Video info",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = screenModel.title,
                    onValueChange = { screenModel.title = it.take(maxTitleLen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocusRequester),
                    singleLine = true,
                    label = { Text("Title") },
                    placeholder = { Text("Add a short, clear title") },
                    supportingText = { Text("$titleLen/$maxTitleLen") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { descriptionFocusRequester.requestFocus() }
                    )
                )

                OutlinedTextField(
                    value = screenModel.description,
                    onValueChange = { screenModel.description = it.take(maxDescriptionLen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .focusRequester(descriptionFocusRequester),
                    label = { Text("Description") },
                    placeholder = { Text("Describe what viewers will see") },
                    minLines = 4,
                    maxLines = 8,
                    supportingText = { Text("$descLen/$maxDescriptionLen") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mark as mature content",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    KSwitch(
                        state = screenModel.nsfw,
                        onCheckedChange = { screenModel.nsfw = it }
                    )
                }
            }
        }
    }
}