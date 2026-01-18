package com.example.edu_smart.components

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun DidYouKnowBanner(
    facts: List<String>,
    modifier: Modifier = Modifier,
    autoRotateMs: Long = 7000L,
    enableTts: Boolean = true
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val backgroundColor = if (isDark) Color(0xFF2D2D2D) else Color(0xFFE6F2FF)
    val textColor = if (isDark) Color(0xFFF5F5F5) else Color(0xFF003B5C)

    // Guard against empty facts to prevent crashes
    val safeFacts = remember(facts) { facts.filter { it.isNotBlank() } }
    var index by rememberSaveable(safeFacts) { mutableStateOf(0) }
    if (safeFacts.isEmpty()) {
        // Graceful empty state
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Text(
                text = "No facts available.",
                modifier = Modifier.padding(16.dp),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    // Text to Speech — init & cleanup
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(enableTts) {
        if (enableTts) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.UK
                    tts?.setSpeechRate(1.0f)
                    tts?.setPitch(1.0f)
                }
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
            tts = null
        }
    }

    // Auto-rotate (cancels/restarts when index or list changes)
    LaunchedEffect(index, safeFacts, autoRotateMs) {
        if (autoRotateMs > 0) {
            delay(autoRotateMs)
            index = (index + 1) % safeFacts.size
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .pointerInput(safeFacts.size) {
                    detectHorizontalDragGestures { _, drag ->
                        when {
                            drag > 30 -> index = (index - 1 + safeFacts.size) % safeFacts.size
                            drag < -30 -> index = (index + 1) % safeFacts.size
                        }
                    }
                }
                .semantics {
                    contentDescription = "Did you know banner"
                }
        ) {
            Text(
                text = "${listOf("💡", "🧠", "📘", "✨", "📚", "🔍", "🚀")[index % 7]} Did You Know?",
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )

            Spacer(Modifier.height(8.dp))

            AnimatedContent(
                targetState = safeFacts[index],
                transitionSpec = {
                    // Slightly snappier fade
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "fact-swap"
            ) { fact ->
                Text(
                    text = fact,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { index = (index + 1) % safeFacts.size }) {
                    Text("Another", color = textColor)
                }
                IconButton(
                    onClick = {
                        if (enableTts) {
                            tts?.speak(safeFacts[index], TextToSpeech.QUEUE_FLUSH, null, "fact-$index")
                        }
                    }
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Speak", tint = textColor)
                }
            }
        }
    }
}
