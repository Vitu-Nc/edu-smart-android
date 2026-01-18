@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.edu_smart.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.edu_smart.api.NewsItem
import com.example.edu_smart.api.RetrofitClient
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

val categories = listOf(
    "world", "technology", "sport", "education", "science", "business", "environment"
)

@Composable
fun NewsScreen(navController: NavController) {
    var articles by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("world") }
    val scope = rememberCoroutineScope()

    fun loadNewsByCategory(category: String) {
        scope.launch {
            isLoading = true
            try {
                val result = RetrofitClient.newsApi.getTopNews(section = category)
                articles = result.response.results
                isError = false
            } catch (e: Exception) {
                e.printStackTrace()
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedCategory) {
        loadNewsByCategory(selectedCategory)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("📰 Latest News") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            CategoryDropdown(
                selectedCategory = selectedCategory,
                onCategorySelected = {
                    selectedCategory = it
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                isError -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Failed to load news. Check your internet connection.")
                    }
                }

                articles.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No news articles available.")
                    }
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(articles) { article ->
                            NewsCard(article = article) {
                                val encodedUrl = URLEncoder.encode(
                                    article.webUrl,
                                    StandardCharsets.UTF_8.toString()
                                )
                                // ✅ FIX: Include /type in the navigation route
                                navController.navigate("webview/$encodedUrl/news")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            readOnly = true,
            value = selectedCategory.replaceFirstChar { it.uppercase() },
            onValueChange = {},
            label = { Text("Select Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun NewsCard(article: NewsItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            article.fields?.thumbnail?.let { thumb ->
                Image(
                    painter = rememberAsyncImagePainter(thumb),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 8.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(article.webTitle, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(article.sectionName, style = MaterialTheme.typography.labelSmall)
                Text(article.webPublicationDate.take(10), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
