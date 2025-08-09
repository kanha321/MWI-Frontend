package com.kanhaji.basics.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.screenModelScope
import com.kanhaji.basics.composables.GenericLazyColumn
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.datastore.PrefsResources
import com.kanhaji.basics.screens.settings.components.ColorPickerDialog
import com.kanhaji.basics.screens.settings.components.ThemeSelectionDialog
import com.kanhaji.basics.screens.settings.components.colorToHex
import com.kanhaji.basics.theme.ThemeManager
import kotlinx.coroutines.launch

@Composable
fun SettingsComponent(
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    // Make the settings list reactive to state changes
    val settingItems by remember {
        derivedStateOf { SettingsScreenModel.getSettingItems() }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(contentPadding)
    ) {
        GenericLazyColumn(
            items = settingItems,
            keySelector = { it.id },
            onItemClick = { settingItems[it].onClick() },
        ) { index, item ->
            SettingsCard(item)
        }

        if (SettingsScreenModel.showThemeDialog) {
            ThemeSelectionDialog(
                onDismiss = { SettingsScreenModel.showThemeDialog = false },
                onConfirm = { SettingsScreenModel.showThemeDialog = false },
                initialSelection = when (ThemeManager.currentThemeType) {
                    ThemeManager.ThemeType.LIGHT -> 0
                    ThemeManager.ThemeType.DARK -> 1
                    ThemeManager.ThemeType.SYSTEM -> 2
                }
            )
        }

        if (SettingsScreenModel.showColorPicker) {
            ColorPickerDialog(
                customColor = ThemeManager.customColor,
                onDismiss = {
                    SettingsScreenModel.showColorPicker = false
                },
                onColorSelected = { color ->
                    ThemeManager.customColor.value = color
                    SettingsScreenModel.showColorPicker = false
                    // Save the selected color to preferences
                    SettingsScreenModel.screenModelScope.launch {
                        PrefsManager.saveString(
                            PrefsResources.CUSTOM_COLOR,
                            colorToHex(color)
                        )
                    }
                }
            )
        }
    }
}