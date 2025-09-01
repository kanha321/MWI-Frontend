package com.mwi.frontend.screens.upload.components.state2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Instructions(

) {
    Column(
        modifier = Modifier.padding(12.dp)
    ) {
        Text(
            text = "How to extract frame:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "1. Play the video above\n2. Pause at your desired frame\n3. Click 'Extract Frame' button",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "💡 Tip:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "For best results, choose a 16:9 aspect ratio thumbnail as it will be displayed in this format on the home screen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}