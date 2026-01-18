package com.example.edu_smart.network

// Root response for the "search" endpoint
data class YouTubeResponse(
    val items: List<VideoDto> = emptyList()
)

data class VideoDto(
    val id: VideoId? = null,
    val snippet: Snippet? = null
)

data class VideoId(
    val kind: String? = null,
    val videoId: String? = null   // can be null for non-video results; we filter these out
)

data class Snippet(
    val title: String? = null,
    val description: String? = null,
    val thumbnails: Thumbnails? = null,
    val channelTitle: String? = null
)

data class Thumbnails(
    val default: Thumbnail? = null,
    val medium: Thumbnail? = null,
    val high: Thumbnail? = null
)

data class Thumbnail(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)
