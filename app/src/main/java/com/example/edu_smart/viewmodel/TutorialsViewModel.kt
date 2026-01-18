package com.example.edu_smart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_smart.api.RetrofitClient
import com.example.edu_smart.model.VideoItem
import com.example.edu_smart.network.VideoMeta
import com.example.edu_smart.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class TutorialsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<TutorialUiState>(TutorialUiState.Idle)
    val uiState: StateFlow<TutorialUiState> = _uiState

    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.value = TutorialUiState.Error("Type something to search.")
            return
        }

        viewModelScope.launch {
            _uiState.value = TutorialUiState.Loading
            try {
                // 1) Search
                val search = RetrofitClient.youTubeApi.searchVideos(
                    q = query,
                    apiKey = Constants.YOUTUBE_API_KEY
                )

                val ids = search.items.mapNotNull { it.id?.videoId }.take(50)
                if (ids.isEmpty()) {
                    _uiState.value = TutorialUiState.Error("No results found.")
                    return@launch
                }

                // 2) Verify embeddability/region
                val meta = RetrofitClient.youTubeApi.getVideosMetadata(
                    ids = ids.joinToString(","),
                    apiKey = Constants.YOUTUBE_API_KEY
                )

                val myCountry = runCatching { Locale.getDefault().country }.getOrDefault("")
                val playableIds = meta.items
                    .filter { metaItem -> isPlayable(metaItem, myCountry) }   // ← named param
                    .map { metaItem -> metaItem.id }
                    .toSet()

                // 3) Build UI list only for verified playable IDs
                val videos: List<VideoItem> = search.items.mapNotNull { item ->
                    val id = item.id?.videoId ?: return@mapNotNull null
                    if (id !in playableIds) return@mapNotNull null

                    val sn = item.snippet ?: return@mapNotNull null
                    val thumbUrl = sn.thumbnails?.medium?.url
                        ?: sn.thumbnails?.high?.url
                        ?: sn.thumbnails?.default?.url
                        ?: ""

                    VideoItem(
                        videoId = id,
                        title = sn.title ?: "(Untitled)",
                        description = sn.description ?: "",
                        thumbnailUrl = thumbUrl
                    )
                }

                _uiState.value =
                    if (videos.isEmpty()) {
                        TutorialUiState.Error(
                            "No videos that can play inside the app were found for “$query”. Try a different search."
                        )
                    } else {
                        TutorialUiState.Success(videos)
                    }
            } catch (e: Exception) {
                _uiState.value = TutorialUiState.Error(
                    "Failed to load videos: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    private fun isPlayable(meta: VideoMeta, country: String): Boolean {
        val status = meta.status
        if (!status.embeddable) return false
        if (status.uploadStatus != "processed") return false
        if (status.privacyStatus != "public") return false

        val rr = meta.contentDetails?.regionRestriction
        val allowed = rr?.allowed
        val blocked = rr?.blocked

        if (country.isNotEmpty() && allowed != null && !allowed.contains(country)) return false
        if (country.isNotEmpty() && blocked != null && blocked.contains(country)) return false

        return true
    }
}
