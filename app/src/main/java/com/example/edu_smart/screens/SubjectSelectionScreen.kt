package com.example.edu_smart.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.edu_smart.models.Subject
import com.example.edu_smart.navigation.Screen

@Composable
fun SubjectSelectionScreen(navController: NavHostController) {


    val subjects = listOf(
        Subject.MALAWI_HISTORY,
        Subject.BIOLOGY,
        Subject.MATHS,
        Subject.AGRICULTURE,
        Subject.RANDOM
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Select a Subject", style = MaterialTheme.typography.headlineSmall)

        subjects.forEach { subject ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.Quiz.withArgs(subject))
                    },
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Subject Title
                    Text(subject.displayName())

                    // Subtitle
                    Text(
                        text = if (subject == Subject.RANDOM)
                            "Mix questions from all subjects"
                        else
                            "Start a ${subject.displayName()} quiz",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// same helper — keeps your display names readable
private fun Subject.displayName(): String =
    name.lowercase()
        .replace("_", " ")
        .replaceFirstChar { it.uppercase() }
