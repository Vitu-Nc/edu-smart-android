package com.example.edu_smart.api

import retrofit2.http.GET
import retrofit2.http.Query

// 🌍 Response wrapper
data class NewsResponse(
    val response: NewsResult
)

data class NewsResult(
    val results: List<NewsItem>
)

data class NewsItem(
    val webTitle: String,
    val webUrl: String,
    val webPublicationDate: String,
    val sectionName: String,
    val fields: NewsFields?
)

data class NewsFields(
    val thumbnail: String?
)

interface NewsApiService {
    @GET("search")
    suspend fun getTopNews(
        @Query("section") section: String = "world",
        @Query("api-key") apiKey: String = "9ae153d4-898b-4ea1-8dbe-3b0098791cd7",
        @Query("show-fields") showFields: String = "thumbnail"
    ): NewsResponse
}

