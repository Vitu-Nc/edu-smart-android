package com.example.edu_smart.model

// ---------- Request ----------
data class GenerateContentRequest(
    val contents: List<Content>
)

data class Content(
    val role: String? = null,     // "user" or "model" (optional but recommended)
    val parts: List<Part>
)

data class Part(
    val text: String
)

// ---------- Response ----------
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList(),
    val promptFeedback: PromptFeedback? = null
)

data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

data class PromptFeedback(
    val blockReason: String? = null
)

// ---------- Helper ----------
fun GenerateContentResponse.firstTextOrNull(): String? {
    val parts = candidates.firstOrNull()?.content?.parts
    return parts?.firstOrNull()?.text
}
