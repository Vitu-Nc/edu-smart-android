package com.example.edu_smart.screens

import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.edu_smart.repository.ChatMessage
import com.example.edu_smart.viewmodel.ChatUiState
import com.example.edu_smart.viewmodel.ChatbotViewModel

@Composable
fun ChatbotScreen() {
    val vm: ChatbotViewModel = viewModel()

    val state by vm.ui.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }

    // 🎙️ Text-to-speech: returns a function we can call to speak text
    val speak = rememberTextToSpeech()

    // 📸 Image picker launcher (Photo Picker)
    val imagePickerLauncher = rememberImagePicker(onImagePicked = { uri ->
        // Hook into your ViewModel for Gemini Vision
        vm.sendImageMessage(uri, prompt = input.ifBlank { null })
        // Optionally clear text input or keep it
    })

    fun sendIfNotBlank() {
        val text = input.trim()
        if (text.isNotEmpty()) {
            vm.sendMessage(text)
            input = ""
        }
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 📸 Button to pick/take a picture
                IconButton(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Send image"
                    )
                }

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask anything…") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendIfNotBlank() })
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { sendIfNotBlank() },
                    enabled = input.isNotBlank()
                ) {
                    Text("Send")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val uiState = state) {
                ChatUiState.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Start the conversation ✨")
                    }
                }

                ChatUiState.Loading -> {
                    MessagesList(messages = emptyList(), onSpeak = speak)
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ChatUiState.Error -> {
                    MessagesList(messages = emptyList(), onSpeak = speak)
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ChatUiState.Success -> {
                    MessagesList(messages = uiState.messages, onSpeak = speak)
                }
            }
        }
    }
}

@Composable
private fun MessagesList(
    messages: List<ChatMessage>,
    onSpeak: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        items(messages) { msg ->
            val isUser = msg.role == "user"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
            ) {
                Surface(
                    color = if (isUser)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )

                        // 🔊 Only show speaker icon for model messages
                        if (!isUser) {
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onSpeak(msg.text) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Read aloud"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🎙️ Helper composable to create & manage a TextToSpeech instance.
 * Returns a lambda you can call as speak("Hello").
 */
@Composable
private fun rememberTextToSpeech(): (String) -> Unit {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    // Init TTS once
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) {
            // You can handle init status here if needed
        }
    }

    // Properly shutdown TTS when this composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    return { text: String ->
        val engine = tts
        if (engine != null) {
            if (engine.isSpeaking) engine.stop()
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "chat-tts")
        }
    }
}

/**
 * 📸 Helper to create a Photo Picker launcher.
 * On some devices, this gives both gallery & camera options.
 */
@Composable
private fun rememberImagePicker(
    onImagePicked: (Uri) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri: Uri? ->
    uri?.let(onImagePicked)
}
