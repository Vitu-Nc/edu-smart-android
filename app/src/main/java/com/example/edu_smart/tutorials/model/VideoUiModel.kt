package com.example.edu_smart.tutorials.model

/**
 * Keep the videoId as the pure ID (not a full URL).
 */
data class VideoUiModel(
    val videoId: String,
    val title: String,
    val thumbnail: String
)