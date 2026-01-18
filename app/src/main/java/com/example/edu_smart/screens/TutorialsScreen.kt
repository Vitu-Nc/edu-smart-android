package com.example.edu_smart.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.edu_smart.model.VideoItem
import com.example.edu_smart.viewmodel.TutorialUiState
import com.example.edu_smart.viewmodel.TutorialsViewModel
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun TutorialsScreen(
    viewModel: TutorialsViewModel = viewModel(),
    onVideoClick: (VideoItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search tutorials") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.search(searchQuery) })
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.search(searchQuery) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is TutorialUiState.Idle -> {
                Text("Type something above and tap search.")
            }

            is TutorialUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is TutorialUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            is TutorialUiState.Success -> {
                val videos = state.videos
                if (videos.isEmpty()) {
                    Text("No results found.")
                } else {
                    LazyColumn {
                        items(
                            items = videos,
                            key = { it.videoId } // stable key for better performance
                        ) { video ->
                            VideoListItem(
                                video = video,
                                onClick = { onVideoClick(video) } // Nav goes to ytplayer/{id}
                            )
                            Divider(thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoListItem(video: VideoItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(video.thumbnailUrl),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = video.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
