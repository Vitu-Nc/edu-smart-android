// screens/BookListScreen.kt
package com.example.edu_smart.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.edu_smart.api.Book
import com.example.edu_smart.api.BookSearchResponse
import com.example.edu_smart.api.RetrofitClient
import com.example.edu_smart.api.bestReaderUrl
import com.example.edu_smart.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun BookListScreen(navController: NavController) {
    var searchQuery by rememberSaveable { mutableStateOf("science") }
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var showOnlyOnlineReadable by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun performSearch() {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            errorMsg = "Please enter a search term."
            books = emptyList()
            return
        }
        scope.launch {
            isLoading = true
            errorMsg = null
            try {
                // ✅ Use named parameter 'query' and request ebooks + availability fields
                val response: BookSearchResponse = RetrofitClient.openLibraryApi.searchBooks(
                    query = q,
                    mode = "ebooks",
                    hasFulltext = true,
                    limit = 40,
                    fields = "key,title,author_name,cover_i,edition_key,ebook_access,ia"
                )
                books = response.docs
            } catch (t: Throwable) {
                errorMsg = t.message ?: "Failed to load results."
                books = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Books") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { performSearch() },
            modifier = Modifier.align(Alignment.End),
            enabled = !isLoading
        ) { Text(if (isLoading) "Searching..." else "Search") }

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Show only readable online")
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = showOnlyOnlineReadable,
                onCheckedChange = { showOnlyOnlineReadable = it }
            )
        }

        Spacer(Modifier.height(8.dp))

        Text("Tap a book to view details or read:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (errorMsg != null) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
        }

        val filteredBooks = remember(books, showOnlyOnlineReadable) {
            if (showOnlyOnlineReadable) {
                books.filter { access ->
                    val a = (access.ebook_access ?: "").lowercase()
                    a == "public" || a == "borrowable"
                }
            } else books
        }

        LazyColumn {
            items(
                items = filteredBooks,
                key = { it.key } // stable key from "/works/OLxxxW"
            ) { book ->
                BookItem(
                    book = book,
                    onClick = {
                        // Open a readable URL if possible (Archive reader or Borrow page)
                        val url = book.bestReaderUrl()
                        navController.navigate(
                            "${Screen.WebView.route}/${Uri.encode(url)}/${Uri.encode("book")}"
                        )
                    },
                    onDetails = {
                        // Navigate to your details screen if you want details view
                        val workId = book.key.removePrefix("/works/")
                        navController.navigate(Screen.BookDetail.withArgs(workId))
                    }
                )
            }
        }
    }
}

@Composable
private fun BookItem(
    book: Book,
    onClick: () -> Unit,
    onDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onDetails() }, // tap card -> details
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(16.dp)) {
            if (book.cover_i != null) {
                Image(
                    painter = rememberAsyncImagePainter("https://covers.openlibrary.org/b/id/${book.cover_i}-M.jpg"),
                    contentDescription = book.title ?: "Book cover",
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // no cover; keep layout stable
                Box(Modifier.size(80.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(book.title ?: "Untitled", fontWeight = FontWeight.Bold)
                Text(book.author_name?.joinToString(", ") ?: "Unknown Author")
                val access = (book.ebook_access ?: "").lowercase()
                val label = when (access) {
                    "public" -> "✅ Readable Online"
                    "borrowable" -> "📘 Borrow Online"
                    else -> "❌ Not Readable Online"
                }
                Text(label, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                // Secondary action: open reader directly
                AssistChip(onClick = onClick, label = { Text("Open") })
            }
        }
    }
}

