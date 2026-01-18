package com.example.edu_smart.model

/**
 * UI-friendly model for tutorial videos.
 * Built from the YouTube API response but simplified
 * so the UI layer doesn’t depend on network structure.
 */
data class VideoItem(
    val videoId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String
)

