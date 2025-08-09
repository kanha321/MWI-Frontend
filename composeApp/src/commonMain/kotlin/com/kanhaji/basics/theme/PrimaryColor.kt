package com.kanhaji.basics.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// gets the primary color from all platforms
@Composable
expect fun getSystemPrimaryColor(): Color