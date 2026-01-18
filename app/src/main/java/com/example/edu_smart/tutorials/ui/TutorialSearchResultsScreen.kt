package com.example.edu_smart.tutorials.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.edu_smart.tutorials.model.VideoUiModel
import com.example.edu_smart.utils.openYouTubeVideo

@Composable
fun TutorialSearchResultsScreen(
    videos: List<VideoUiModel>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(videos) { v ->
            VideoResultItem(v)
        }
    }
}

@Composable
private fun VideoResultItem(video: VideoUiModel) {
    val context = LocalContext.current

    Card(
        onClick = { context.openYouTubeVideo(video.videoId) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            AsyncImage(
                model = video.thumbnail,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            // Title
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
