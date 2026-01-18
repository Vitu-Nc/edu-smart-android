package com.example.edu_smart.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.edu_smart.components.DidYouKnowBanner
import com.example.edu_smart.navigation.Screen
import com.example.edu_smart.utils.scienceFacts
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🚪 Logout button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    FirebaseAuth.getInstance().signOut()

                    // Clear backstack and return to login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Text("Log out")
            }
        }

        // Trivia Banner
        DidYouKnowBanner(facts = scienceFacts)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Welcome to EDU-SMART!",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModuleButton("Library") { navController.navigate(Screen.Library.route) }
            ModuleButton("Chatbot") { navController.navigate(Screen.Chatbot.route) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModuleButton("Quiz") {
                navController.navigate(Screen.SubjectPicker.route)
            }
            ModuleButton("Tutorials") { navController.navigate(Screen.Tutorials.route) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ModuleButton("News") { navController.navigate(Screen.News.route) }
        }
    }
}

@Composable
fun ModuleButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .height(80.dp)
    ) {
        Text(text)
    }
}


