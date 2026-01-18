package com.example.edu_smart.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.edu_smart.api.Book
import com.example.edu_smart.api.BookSearchResponse
import com.example.edu_smart.api.RetrofitClient
import com.example.edu_smart.models.FirebaseBook
import com.example.edu_smart.navigation.Screen
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Filter options for Open Library ebooks
enum class EbookFilter { ALL, READABLE, BORROW }

// --- Helper extension to read pdfurl from different field names ---
private fun DocumentSnapshot.getPdfUrl(): String? {
    return getString("pdfurl")
        ?: getString("pdfUrl")
        ?: getString("url")
}

// --- Helper: download a Firebase PDF to user's phone -------------------------
private fun downloadBookPdf(context: Context, url: String, title: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(title.ifBlank { "EDU-SMART Book" })
            .setDescription("Downloading book…")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setAllowedOverMetered(true)

        // Sanitize file name a bit
        val safeFileName =
            (if (title.isBlank()) "edu_smart_book" else title)
                .replace(Regex("[^A-Za-z0-9._-]"), "_") + ".pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // App-specific Downloads folder (no extra permissions needed)
            request.setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                safeFileName
            )
        } else {
            @Suppress("DEPRECATION")
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                safeFileName
            )
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)

        Toast.makeText(context, "Download started…", Toast.LENGTH_SHORT).show()
    } catch (t: Throwable) {
        Toast.makeText(
            context,
            "Failed to start download: ${t.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}
// -----------------------------------------------------------------------------


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavHostController) {

    val context = LocalContext.current

    // 🔹 Tabs: 0 = My Library (Firebase), 1 = Online Library (Open Library)
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    // 🔍 Open Library search state
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var bookList by remember { mutableStateOf<List<Book>>(emptyList()) }
    var searchLoading by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    // 🔻 Filter for Open Library
    var ebookFilter by rememberSaveable { mutableStateOf(EbookFilter.ALL) }

    // 🔐 Firebase books
    var firebaseBooks by remember { mutableStateOf<List<FirebaseBook>>(emptyList()) }
    var firebaseLoading by remember { mutableStateOf(true) }
    var firebaseError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val db = Firebase.firestore

    // 🔄 Load ALL Firebase books once (top level + subcollections)
    LaunchedEffect(Unit) {
        try {
            val result = mutableListOf<FirebaseBook>()
            val topSnapshot = db.collection("library").get().await()

            // 1) Docs directly in "library" (Software_Engineering, computer_network, etc.)
            for (doc in topSnapshot.documents) {
                val title = doc.getString("title") ?: doc.getString("Title")
                val url = doc.getPdfUrl()

                if (!title.isNullOrEmpty() && !url.isNullOrEmpty()) {
                    result += FirebaseBook(title = title, pdfurl = url)
                }

                // 2) Unit docs inside subcollections.
                // In your DB, names can be "computer_network" or "Computer_network".
                val categoryId = doc.id
                val candidates = listOf(
                    categoryId,
                    categoryId.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }
                ).distinct()

                for (collectionId in candidates) {
                    val subSnapshot = doc.reference
                        .collection(collectionId)
                        .get()
                        .await()

                    for (bookDoc in subSnapshot.documents) {
                        val unitTitle = bookDoc.getString("title")
                            ?: bookDoc.getString("Title")
                            ?: bookDoc.id
                        val unitUrl = bookDoc.getPdfUrl()

                        if (!unitTitle.isNullOrEmpty() && !unitUrl.isNullOrEmpty()) {
                            result += FirebaseBook(title = unitTitle, pdfurl = unitUrl)
                        }
                    }
                }
            }

            firebaseBooks = result
        } catch (t: Throwable) {
            firebaseError = t.message ?: "Failed to load your library."
        } finally {
            firebaseLoading = false
        }
    }

    fun performSearch() {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            searchError = "Please enter a search term."
            bookList = emptyList()
            return
        }
        scope.launch {
            searchLoading = true
            searchError = null
            try {
                val result: BookSearchResponse = RetrofitClient.openLibraryApi.searchBooks(
                    query = q,
                    mode = "ebooks",
                    hasFulltext = true,
                    limit = 40,
                    fields = "key,title,author_name,cover_i,edition_key,ebook_access,ia"
                )
                bookList = result.docs
            } catch (t: Throwable) {
                searchError = t.message ?: "Failed to load books. Check your connection."
                bookList = emptyList()
            } finally {
                searchLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 🔝 Tabs
        val tabs = listOf("My Library", "Online Library")
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when (selectedTab) {
            // ===========================
            //  TAB 1: MY LIBRARY (FIREBASE)
            // ===========================
            0 -> {
                if (firebaseLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (firebaseError != null) {
                    Text(
                        text = firebaseError!!,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (firebaseBooks.isEmpty()) {
                    Text("No books in your library yet.")
                } else {
                    LazyColumn {
                        items(firebaseBooks, key = { "${it.title}-${it.pdfurl}" }) { book ->
                            FirebaseBookCard(book) {
                                val url = book.pdfurl
                                val lastSegment = Uri.parse(url).lastPathSegment ?: ""
                                val ext = lastSegment.substringAfterLast('.', "").lowercase()

                                if (ext == "pdf") {
                                    // ✅ Just download the PDF to the user's phone
                                    downloadBookPdf(
                                        context = context,
                                        url = url,
                                        title = book.title
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Only PDF files are supported. Please upload a PDF version of this book.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }

            // ===========================
            //  TAB 2: ONLINE LIBRARY (OPEN LIBRARY)
            // ===========================
            1 -> {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Open Library") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { performSearch() })
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { performSearch() },
                        enabled = !searchLoading
                    ) {
                        Text(if (searchLoading) "Searching..." else "Search")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = ebookFilter == EbookFilter.ALL,
                            onClick = { ebookFilter = EbookFilter.ALL },
                            label = { Text("All") }
                        )
                        FilterChip(
                            selected = ebookFilter == EbookFilter.READABLE,
                            onClick = { ebookFilter = EbookFilter.READABLE },
                            label = { Text("Readable") }
                        )
                        FilterChip(
                            selected = ebookFilter == EbookFilter.BORROW,
                            onClick = { ebookFilter = EbookFilter.BORROW },
                            label = { Text("Borrow") }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                val filteredBooks = remember(bookList, ebookFilter) {
                    bookList.filter { book ->
                        val access = (book.ebook_access ?: "").lowercase()
                        when (ebookFilter) {
                            EbookFilter.ALL      -> true
                            EbookFilter.READABLE -> access == "public"
                            EbookFilter.BORROW   -> access == "borrowable"
                        }
                    }
                }

                LazyColumn {
                    when {
                        searchLoading -> item {
                            Box(
                                Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        searchError != null -> item {
                            Text(
                                text = searchError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        filteredBooks.isEmpty() -> item {
                            Text("No results. Try another search or filter.")
                        }

                        else -> items(filteredBooks, key = { it.key }) { book ->
                            BookListItem(book = book) {
                                val access = (book.ebook_access ?: "").lowercase()
                                when {
                                    access == "public" && !book.ia.isNullOrEmpty() -> {
                                        val readerUrl =
                                            "https://archive.org/details/${book.ia.first()}?view=theater"
                                        navController.navigate(
                                            "${Screen.WebView.route}/${Uri.encode(readerUrl)}/${Uri.encode("book")}"
                                        )
                                    }

                                    access == "borrowable" && !book.edition_key.isNullOrEmpty() -> {
                                        val borrowUrl =
                                            "https://openlibrary.org/books/${book.edition_key.first()}"
                                        navController.navigate(
                                            "${Screen.WebView.route}/${Uri.encode(borrowUrl)}/${Uri.encode("book")}"
                                        )
                                    }

                                    else -> {
                                        val workUrl = "https://openlibrary.org${book.key}"
                                        navController.navigate(
                                            "${Screen.WebView.route}/${Uri.encode(workUrl)}/${Uri.encode("book")}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============ ITEM UI ============

@Composable
fun BookListItem(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(12.dp)) {
            book.cover_i?.let { coverId ->
                Image(
                    painter = rememberAsyncImagePainter("https://covers.openlibrary.org/b/id/$coverId-M.jpg"),
                    contentDescription = "Book Cover",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 12.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(book.title ?: "Untitled", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = book.author_name?.joinToString(", ") ?: "Unknown Author",
                    style = MaterialTheme.typography.bodySmall
                )
                val badge = when ((book.ebook_access ?: "").lowercase()) {
                    "public"     -> "Readable"
                    "borrowable" -> "Borrow"
                    else         -> null
                }
                if (badge != null) {
                    Spacer(Modifier.height(6.dp))
                    AssistChip(onClick = onClick, label = { Text(badge) })
                }
            }
        }
    }
}

@Composable
fun FirebaseBookCard(book: FirebaseBook, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(book.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Firebase Resource", style = MaterialTheme.typography.labelSmall)
        }
    }
}

