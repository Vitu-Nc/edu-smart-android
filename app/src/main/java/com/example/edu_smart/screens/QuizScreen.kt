@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.edu_smart.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.edu_smart.models.*
import com.example.edu_smart.quiz.QuizRepository
import kotlin.math.max
import kotlinx.coroutines.delay   // 👈 added

@Composable
fun QuizScreen(
    subject: Subject,
    questionCount: Int = 10,
    difficulty: Difficulty = Difficulty.MEDIUM,
    onExit: () -> Unit = {}
) {
    // ✅ Hoist the context and key remember with it.
    val context = LocalContext.current
    val repo = remember(context) { QuizRepository(context) }

    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var idx by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableIntStateOf(-1) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    var finished by rememberSaveable { mutableStateOf(false) }
    var fiftyUsed by rememberSaveable { mutableStateOf(false) }
    var maskedOptions by remember { mutableStateOf<Set<Int>>(emptySet()) }

    // Emoji feedback flags
    var showFeedback by rememberSaveable { mutableStateOf(false) }
    var wasCorrect by rememberSaveable { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(subject, questionCount, difficulty, repo) {
        loading = true
        questions = repo.fetch(subject, questionCount, difficulty)
        idx = 0; selected = -1; score = 0; finished = false
        fiftyUsed = false; maskedOptions = emptySet()
        showFeedback = false; wasCorrect = null
        loading = false
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No questions found for ${subject.name.replace('_',' ')}")
        }
        return
    }

    val q = questions[idx]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${subject.name.replace('_',' ')} • Q${idx + 1}/${questions.size}") },
                actions = {
                    AssistChip(
                        onClick = {
                            if (!fiftyUsed && !showFeedback && !finished) {
                                fiftyUsed = true
                                maskedOptions = maskTwoWrong(q)
                            }
                        },
                        label = { Text(if (fiftyUsed) "50/50 used" else "50/50") },
                        enabled = !fiftyUsed && !showFeedback && !finished
                    )
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Text(q.question, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            q.options.forEachIndexed { i, option ->
                val isMasked = i in maskedOptions

                val bgColor = when {
                    showFeedback && i == q.correctAnswerIndex ->
                        MaterialTheme.colorScheme.secondaryContainer
                    showFeedback && i == selected && i != q.correctAnswerIndex ->
                        MaterialTheme.colorScheme.errorContainer
                    selected == i ->
                        MaterialTheme.colorScheme.primaryContainer
                    else ->
                        MaterialTheme.colorScheme.surface
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .let { m ->
                            if (!showFeedback && !isMasked) m.clickable { selected = i } else m
                        },
                    colors = CardDefaults.cardColors(containerColor = bgColor)
                ) {
                    Box(Modifier.padding(14.dp)) {
                        val prefix = when {
                            showFeedback && i == q.correctAnswerIndex -> "✅ "
                            showFeedback && i == selected && i != q.correctAnswerIndex -> "❌ "
                            else -> ""
                        }
                        Text(if (isMasked) "—" else prefix + option)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (showFeedback && wasCorrect != null) {
                val msg = if (wasCorrect == true) "✅ Correct! Nice job 😎"
                else "❌ Not quite. Keep going—you’ve got this! 💪"
                Text(msg, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                q.explanation?.let {
                    Text("ℹ️ $it", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onExit) { Text("Exit") }

                val isLast = idx == questions.lastIndex
                val btnLabel = when {
                    finished -> "Restart"
                    !showFeedback -> "Check Answer"
                    showFeedback && !isLast -> "Next Question"
                    else -> "See Results"
                }

                Button(
                    onClick = {
                        when {
                            finished -> {
                                questions = repo.fetch(subject, questionCount, difficulty)
                                idx = 0; selected = -1; score = 0; finished = false
                                fiftyUsed = false; maskedOptions = emptySet()
                                showFeedback = false; wasCorrect = null
                            }
                            !showFeedback -> {
                                if (selected != -1) {
                                    val correct = selected == q.correctAnswerIndex
                                    wasCorrect = correct
                                    if (correct) score++
                                    showFeedback = true
                                }
                            }
                            showFeedback && !isLast -> {
                                idx += 1
                                selected = -1
                                maskedOptions = emptySet()
                                fiftyUsed = false
                                showFeedback = false
                                wasCorrect = null
                            }
                            else -> {
                                finished = true
                            }
                        }
                    },
                    enabled = when {
                        finished -> true
                        !showFeedback -> selected != -1
                        else -> true
                    }
                ) { Text(btnLabel) }
            }

            // 🔄 Auto-advance after showing feedback (with small delay)
            LaunchedEffect(showFeedback, idx, finished) {
                if (showFeedback && !finished) {
                    delay(1200L)  // allow time to see feedback / explanation

                    // Check again in case user already tapped "Next" or "See Results"
                    if (showFeedback && !finished) {
                        val lastIndex = questions.lastIndex
                        if (idx < lastIndex) {
                            idx += 1
                            selected = -1
                            maskedOptions = emptySet()
                            fiftyUsed = false
                            showFeedback = false
                            wasCorrect = null
                        } else {
                            finished = true
                        }
                    }
                }
            }

            if (finished) {
                Spacer(Modifier.height(20.dp))
                ResultCard(score, questions.size) {
                    questions = repo.fetch(subject, questionCount, difficulty)
                    idx = 0; selected = -1; score = 0; finished = false
                    fiftyUsed = false; maskedOptions = emptySet()
                    showFeedback = false; wasCorrect = null
                }
            }
        }
    }
}

@Composable
private fun ResultCard(score: Int, total: Int, onRestart: () -> Unit) {
    val pct = (score * 100) / max(1, total)
    val face = when {
        pct >= 90 -> "🏆"
        pct >= 70 -> "🎉"
        pct >= 50 -> "👍"
        else -> "🌱"
    }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$face Quiz Finished", style = MaterialTheme.typography.titleLarge)
            Text("Score: $score / $total ($pct%)")
            LinearProgressIndicator(progress = pct / 100f)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onRestart) { Text("Try Again") }
            }
        }
    }
}

private fun maskTwoWrong(q: QuizQuestion): Set<Int> {
    val wrongs = q.options.indices.filter { it != q.correctAnswerIndex }.shuffled()
    return wrongs.take(2).toSet()
}
