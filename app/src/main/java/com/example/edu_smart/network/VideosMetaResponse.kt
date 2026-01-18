package com.example.edu_smart.network

data class VideosMetaResponse(val items: List<VideoMeta> = emptyList())
data class VideoMeta(
    val id: String,
    val status: VideoStatus,
    val contentDetails: ContentDetails?
)
data class VideoStatus(
    val embeddable: Boolean,
    val uploadStatus: String,
    val privacyStatus: String,
    val madeForKids: Boolean? = null
)
data class ContentDetails(val regionRestriction: RegionRestriction? = null)
data class RegionRestriction(
    val allowed: List<String>? = null,
    val blocked: List<String>? = null
)
