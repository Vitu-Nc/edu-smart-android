package com.example.edu_smart.api

import com.example.edu_smart.model.GenerateContentRequest
import com.example.edu_smart.model.GenerateContentResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeminiService {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String = "gemini-2.0-flash",   // or gemini-2.0-pro, depending on your key
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}
