package com.example.edu_smart.api

import com.example.edu_smart.network.YouTubeApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ✅ Shared OkHttpClient — adds timeouts for reliability
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    // ✅ YouTube API client
    val youTubeApi: YouTubeApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YouTubeApiService::class.java)
    }

    // ✅ Open Library API client (for Books)
    val openLibraryApi: OpenLibraryApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://openlibrary.org/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenLibraryApi::class.java)
    }

    // ✅ Guardian News API client
    val newsApi: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://content.guardianapis.com/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }

    // ✅ Gemini API client (for Chatbot)
    val gemini: GeminiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/") // 👈 no v1beta here
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiService::class.java)
    }

}

