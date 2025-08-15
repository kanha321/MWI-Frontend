package com.mwi.frontend.ui.components

import android.util.Log
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mwi.frontend.entity.VideoMetadata
import com.mwi.frontend.util.Resources

@Composable
fun VideoItem(videoMetadata: VideoMetadata) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),
        onClick = {

        },
        content = {
//             Use aspectRatio to set 16:9 ratio
//            Image(
//                painter = rememberAsyncImagePainter(model = imageUrl),
//                contentDescription = null,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .aspectRatio(16f / 9f),
//                contentScale = ContentScale.Crop
//            )
            AsyncImage(
                model = Resources.BASE_URL + videoMetadata.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Text(
                text = videoMetadata.title,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp)
            )
        },
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.secondaryContainer
//        )
    )
}