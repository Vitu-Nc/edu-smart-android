package com.example.edu_smart.repository

import android.net.Uri
import com.example.edu_smart.api.GeminiService
import com.example.edu_smart.api.GeminiStreamingClient
import com.example.edu_smart.model.Content
import com.example.edu_smart.model.GenerateContentRequest
import com.example.edu_smart.model.Part
import com.example.edu_smart.model.firstTextOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient

data class ChatMessage(val role: String, val text: String) // "user" | "model"

class GeminiChatRepository(
    private val api: GeminiService,
    private val apiKey: String,
    private val streamingClient: GeminiStreamingClient = GeminiStreamingClient(OkHttpClient())
) {
    /** Non-streaming single-shot call. */
    suspend fun sendChat(messages: List<ChatMessage>): String {
        // ✅ Multi-turn with roles
        val req = GenerateContentRequest(contents = messages.toContents())

        return try {
            val resp = api.generateContent(
                apiKey = apiKey,
                request = req
            )
            resp.firstTextOrNull()?.trim()
                ?: "Sorry, I couldn’t generate a response."
        } catch (e: retrofit2.HttpException) {
            val body = e.response()?.errorBody()?.string()
            println("Gemini HTTP ERROR ${e.code()} – ${e.message()} – $body")
            "Error: HTTP ${e.code()}"
        } catch (t: Throwable) {
            println("Gemini ERROR: ${t.message}")
            "Error: ${t.message ?: "Unknown error"}"
        }
    }

    /**
     * 📸 Image + text chat.
     *
     * NOTE:
     *  - This implementation does NOT yet send raw image bytes to Gemini Vision.
     *  - It augments the conversation with a textual note that an image was sent,
     *    plus any optional prompt, and then uses the normal text model.
     *
     * Once your `Part` / `Content` models support inlineData for images,
     * you can replace this body with a real Vision request.
     */
    suspend fun sendImageChat(
        imageUri: Uri,
        prompt: String?,
        history: List<ChatMessage>
    ): String {
        val promptText = prompt?.takeIf { it.isNotBlank() }

        // Build a synthetic user message describing the image.
        val imageMessageText = buildString {
            append("The user has sent an image (URI: ")
            append(imageUri.toString())
            append("). ")
            if (promptText != null) {
                append("Additional instruction from the user: ")
                append(promptText)
            } else {
                append("No extra text prompt was provided.")
            }
        }

        val extendedHistory = history + ChatMessage(
            role = "user",
            text = imageMessageText
        )

        // Reuse the existing text-only pipeline for now
        return sendChat(extendedHistory)
    }

    /** Streaming version (optional). */
    fun streamChat(messages: List<ChatMessage>): Flow<String> {
        val trimmed = messages.trimToLastMessages(maxMessages = 16)
        val req = GenerateContentRequest(contents = trimmed.toContents())

        return streamingClient
            .streamGenerateContent(
                apiKey = apiKey,
                request = req
            )
            .map { chunk -> chunk.firstTextOrNull().orEmpty() }
    }

    // --- helpers ---

    private fun List<ChatMessage>.toContents(): List<Content> =
        this.map { msg ->
            Content(
                role = if (msg.role == "model") "model" else "user",
                parts = listOf(Part(text = msg.text))
            )
        }

    private fun List<ChatMessage>.trimToLastMessages(maxMessages: Int): List<ChatMessage> =
        if (this.size <= maxMessages) this else this.takeLast(maxMessages)
}
