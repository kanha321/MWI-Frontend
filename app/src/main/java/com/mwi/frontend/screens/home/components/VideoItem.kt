package com.mwi.frontend.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mwi.frontend.entity.VideoMetadata
import com.mwi.frontend.util.AppSettingsItems
import com.mwi.frontend.util.MwiUtils
import com.mwi.frontend.util.MwiUtils.formatTime
import com.mwi.frontend.util.MwiUtils.formatUploadDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoItem(
    videoMetadata: VideoMetadata,
    onClick: () -> Unit = {}
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Thumbnail section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    model = MwiUtils.BASE_URL + videoMetadata.thumbnailUrl,
                    contentDescription = "Video thumbnail",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .then(
                            if (videoMetadata.nsfw && !AppSettingsItems.showNsfwContent) Modifier.blur(
                                30.dp
                            ) else Modifier
                        ),
                    contentScale = ContentScale.Crop
                )

                // Duration badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = formatTime(videoMetadata.duration),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // NSFW overlay (center of screen)
                if (videoMetadata.nsfw && !AppSettingsItems.showNsfwContent) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "NSFW content",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Video info section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Creator avatar with person icon
                Surface(
                    modifier = Modifier
                        .size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Creator",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Video details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = videoMetadata.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Upload date and views
                    Text(
                        text = formatUploadDate(videoMetadata.uploadDate) +
                                " • " +
                                videoMetadata.views + " views",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
