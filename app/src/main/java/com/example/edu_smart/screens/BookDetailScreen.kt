@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.edu_smart.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.edu_smart.api.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun BookDetailScreen(navController: NavController, workId: String) {
    var title by remember { mutableStateOf("Loading...") }
    var description by remember { mutableStateOf("Please wait...") }
    var subjects by remember { mutableStateOf<List<String>>(emptyList()) }
    var readUrl by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(workId) {
        scope.launch {
            try {
                val details = RetrofitClient.openLibraryApi.getBookDetails(workId)
                title = details.title ?: "No Title"
                description = when (val desc = details.description) {
                    is String -> desc
                    is Map<*, *> -> desc["value"]?.toString() ?: "No description available."
                    else -> "No description available."
                }
                subjects = details.subjects ?: emptyList()

                val editions = RetrofitClient.openLibraryApi.getBookEditions(workId)
                readUrl = editions.entries.firstOrNull { it.preview_url != null }?.preview_url
                    ?: editions.entries.firstOrNull()?.url

                Log.d("BookDetailScreen", "Fetched preview URL: $readUrl")
            } catch (e: Exception) {
                Log.e("BookDetailScreen", "Error loading book details: ${e.message}")
                description = "⚠️ Failed to load book details."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Description:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = description)
            Spacer(modifier = Modifier.height(16.dp))

            if (subjects.isNotEmpty()) {
                Text(
                    text = "Subjects:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                subjects.forEach {
                    Text(text = "- $it")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            readUrl?.let { url ->
                Button(
                    onClick = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("webUrl", url)
                        navController.currentBackStackEntry?.savedStateHandle?.set("webType", "Book")
                        navController.navigate("webview")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📖 Read Book")
                }
            } ?: Text(
                text = "❌ This book is not readable online.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
