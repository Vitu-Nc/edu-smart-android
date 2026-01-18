package com.example.edu_smart.network

import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") q: String,
        @Query("type") type: String = "video",
        @Query("videoEmbeddable") embeddable: String = "true",
        @Query("videoSyndicated") syndicated: String = "true",
        @Query("safeSearch") safeSearch: String = "strict",
        @Query("maxResults") maxResults: Int = 25,
        @Query("key") apiKey: String
    ): YouTubeResponse

    // ✅ Add this method (used by your ViewModel)
    @GET("videos")
    suspend fun getVideosMetadata(
        @Query("part") part: String = "status,contentDetails",
        @Query("id") ids: String,                 // comma-separated up to 50
        @Query("key") apiKey: String
    ): VideosMetaResponse
}
