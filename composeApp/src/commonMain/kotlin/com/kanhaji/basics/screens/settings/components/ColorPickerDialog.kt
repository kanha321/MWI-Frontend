package com.kanhaji.basics.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.kanhaji.basics.theme.ThemeManager

@Composable
fun ColorPickerDialog(
    customColor: MutableState<Color>, // Pass external state here
    initialColor: Color = ThemeManager.customColor.value,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val initialColor by remember { mutableStateOf(customColor.value) }
    var selectedColor by remember { mutableStateOf(customColor.value) }
    var hexCode by remember { mutableStateOf(colorToHex(customColor.value)) }

    val controller = rememberColorPickerController()

    // Sync TextField when color changes (from picker)
    LaunchedEffect(selectedColor) {
        val newHex = colorToHex(selectedColor)
        if (newHex != hexCode) {
            hexCode = newHex
        }
        customColor.value = selectedColor // Update external variable live
    }

    // Sync Color Picker when hex changes
    LaunchedEffect(hexCode) {
        if (hexCode.length == 6) { // Only trigger parsing when 6 characters are entered
            try {
                val newColor = hexToColor(hexCode)
                if (newColor != selectedColor) {
                    selectedColor = newColor
                    customColor.value = newColor // Update external variable live
                    controller.selectByColor(newColor, fromUser = false)
                }
            } catch (_: Exception) {
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick a Color") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // HSV Color Picker
                HsvColorPicker(
                    initialColor = initialColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(10.dp),
                    controller = controller,
                    onColorChanged = { colorEnvelope ->
                        selectedColor = colorEnvelope.color
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hex Code TextField
                OutlinedTextField(
                    value = hexCode,
                    onValueChange = { newHex ->
                        val filteredHex = newHex.filter { it.isDigit() || it in "abcdefABCDEF" } // Allow only valid hex characters
                        if (filteredHex.length <= 6) {
                            hexCode = filteredHex.uppercase() // Convert to uppercase for consistency
                        }
                    },
                    label = { Text("Hex Code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii
                    ),
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(selectedColor, shape = CircleShape)
                                .border(1.dp, Color.Gray, CircleShape)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(selectedColor); onDismiss() }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                customColor.value = initialColor // Reset to initial color on cancel
            }) {
                Text("Cancel")
            }
        }
    )
}

fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    return (argb and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()
}


fun hexToColor(hex: String): Color {
    return try {
        val parsedColor = hex.toLong(16) or 0xFF000000 // Ensure alpha = 255 (opaque)
        Color(parsedColor)
    } catch (e: Exception) {
        Color(0xFF8F4C38) // Fallback color
    }
}
