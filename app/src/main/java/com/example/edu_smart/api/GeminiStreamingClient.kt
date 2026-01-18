package com.example.edu_smart.api

import com.example.edu_smart.model.GenerateContentRequest
import com.example.edu_smart.model.GenerateContentResponse
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

/**
 * Streams text chunks from Gemini via SSE.
 * Endpoint: POST models/gemini-1.5-flash:streamGenerateContent?alt=sse&key=API_KEY
 */
class GeminiStreamingClient(
    private val httpClient: OkHttpClient,
    private val baseUrl: String = "https://generativelanguage.googleapis.com/v1beta/",
    private val gson: Gson = Gson()
) {

    fun streamGenerateContent(apiKey: String, request: GenerateContentRequest): Flow<GenerateContentResponse> {
        val url = "${baseUrl}models/gemini-1.5-flash:streamGenerateContent?alt=sse&key=$apiKey"
        val json = gson.toJson(request)
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        return callbackFlow {
            val req = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val factory = EventSources.createFactory(httpClient)
            val es = factory.newEventSource(req, object : EventSourceListener() {
                override fun onEvent(source: EventSource, id: String?, type: String?, data: String) {
                    // Each event "data:" line is a JSON chunk that matches GenerateContentResponse shape
                    try {
                        val chunk = gson.fromJson(data, GenerateContentResponse::class.java)
                        trySend(chunk).isSuccess
                    } catch (_: Throwable) {
                        // Ignore parsing errors for keepalives or non-json lines
                    }
                }
                override fun onClosed(source: EventSource) {
                    close()
                }
                override fun onFailure(source: EventSource, t: Throwable?, response: okhttp3.Response?) {
                    close(t ?: IllegalStateException("SSE failed"))
                }
            })

            awaitClose { es.cancel() }
        }
    }
}
