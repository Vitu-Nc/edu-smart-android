@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.edu_smart.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.edu_smart.models.Subject

@Composable
fun SubjectPickerScreen(onSubjectSelected: (Subject) -> Unit) {
    val subjects = remember {
        listOf(
            Subject.MALAWI_HISTORY,
            Subject.BIOLOGY,
            Subject.MATHS,
            Subject.AGRICULTURE,
            Subject.RANDOM
        )
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Choose a Subject") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(subjects.size) { i ->
                val s = subjects[i]
                ElevatedCard(onClick = { onSubjectSelected(s) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(s.displayName())
                        Text(
                            text = if (s == Subject.RANDOM) {
                                "Mix questions from all subjects"
                            } else {
                                "Start a ${s.displayName()} quiz"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// Helper to make enum names look nice
private fun Subject.displayName(): String =
    name.lowercase()
        .replace("_", " ")
        .replaceFirstChar { it.uppercase() }
