package com.example.edu_smart.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edu_smart.BuildConfig
import com.example.edu_smart.api.RetrofitClient
import com.example.edu_smart.repository.ChatMessage
import com.example.edu_smart.repository.GeminiChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ChatUiState {
    data object Idle : ChatUiState()
    data object Loading : ChatUiState()
    data class Error(val message: String) : ChatUiState()
    data class Success(val messages: List<ChatMessage>) : ChatUiState()
}

class ChatbotViewModel : ViewModel() {   // 👈 no constructor args now

    private val repo = GeminiChatRepository(
        api = RetrofitClient.gemini,
        apiKey = BuildConfig.GEMINI_API_KEY   // 👈 read from BuildConfig
    )

    private val _ui = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val ui: StateFlow<ChatUiState> = _ui

    private val history = mutableListOf<ChatMessage>()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // add user message and show it immediately
        history += ChatMessage(role = "user", text = text)
        _ui.value = ChatUiState.Success(history.toList())

        viewModelScope.launch {
            _ui.value = ChatUiState.Loading
            try {
                val reply = repo.sendChat(history)
                history += ChatMessage(role = "model", text = reply)
                _ui.value = ChatUiState.Success(history.toList())
            } catch (t: Throwable) {
                _ui.value = ChatUiState.Error(t.message ?: "Failed to reach Gemini")
            }
        }
    }

    /**
     * 📸 Called when the user picks/sends an image (from ChatbotScreen).
     *
     * @param imageUri  The URI of the selected image.
     * @param prompt    Optional extra text the user typed with the image.
     */
    fun sendImageMessage(imageUri: Uri, prompt: String? = null) {
        viewModelScope.launch {
            val userText = prompt?.takeIf { it.isNotBlank() } ?: "🖼️ Sent an image"

            // Show something in the chat history for the user
            history += ChatMessage(role = "user", text = userText)
            _ui.value = ChatUiState.Success(history.toList())

            _ui.value = ChatUiState.Loading
            try {
                // 👉 You’ll implement this in GeminiChatRepository
                //    (e.g., calling Gemini Vision with image + optional text)
                val reply = repo.sendImageChat(
                    imageUri = imageUri,
                    prompt = prompt,
                    history = history.toList()
                )

                history += ChatMessage(role = "model", text = reply)
                _ui.value = ChatUiState.Success(history.toList())
            } catch (t: Throwable) {
                _ui.value = ChatUiState.Error(t.message ?: "Failed to send image to Gemini")
            }
        }
    }
}

