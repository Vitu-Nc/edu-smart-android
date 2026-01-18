@file:Suppress("DEPRECATION")

package com.example.edu_smart.screens

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    navController: NavController,
    url: String,
    titleType: String
) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isLoading by rememberSaveable { mutableStateOf(true) }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Back: exit fullscreen > web history > pop
    BackHandler(enabled = (customView != null) || (webViewRef?.canGoBack() == true)) {
        when {
            customView != null -> {
                customViewCallback?.onCustomViewHidden()
                customView = null
                customViewCallback = null
            }
            webViewRef?.canGoBack() == true -> webViewRef?.goBack()
            else -> navController.popBackStack()
        }
    }

    val displayTitle = remember(titleType) {
        when (titleType.lowercase(Locale.ROOT)) {
            "book" -> "Book"
            "news" -> "News Article"
            else -> titleType.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
            }
        }
    }

    // --- URL helpers ---------------------------------------------------------

    // Only care if it's actually a .pdf
    fun isPdfUrl(u: String): Boolean {
        val last = Uri.parse(u).lastPathSegment.orEmpty().lowercase(Locale.ROOT)
        return last.endsWith(".pdf")
    }

    fun toYoutubeEmbed(u: String): String? {
        val lower = u.lowercase(Locale.ROOT)
        val id = when {
            "watch?v=" in lower -> Uri.parse(u).getQueryParameter("v")
            "youtu.be/" in lower -> lower.substringAfter("youtu.be/").substringBefore('?').substringBefore('&')
            "/shorts/" in lower -> lower.substringAfter("/shorts/").substringBefore('?').substringBefore('&')
            else -> null
        }
        return id?.let { "https://www.youtube.com/embed/$it?playsinline=1" }
    }

    // No more docs.google.com/gview wrapping for Firebase PDFs
    val finalUrl = remember(url) {
        when {
            toYoutubeEmbed(url) != null ->
                toYoutubeEmbed(url)!!

            // Load PDFs (like your Firebase books) directly
            isPdfUrl(url) ->
                url

            else ->
                url
        }
    }
    // -------------------------------------------------------------------------

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Read $displayTitle") },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            customView != null -> {
                                customViewCallback?.onCustomViewHidden()
                                customView = null
                                customViewCallback = null
                            }
                            webViewRef?.canGoBack() == true -> webViewRef?.goBack()
                            else -> navController.popBackStack()
                        }
                    }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    // 🌐 Open in external browser
                    IconButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }) { Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in browser") }

                    // Show download for books / PDFs
                    if (titleType.equals("book", ignoreCase = true) || isPdfUrl(url)) {
                        IconButton(onClick = {
                            val req = DownloadManager.Request(Uri.parse(url))
                                .setTitle("Downloading $displayTitle")
                                .setDescription("Saving…")
                                .setNotificationVisibility(
                                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                                )
                                .setAllowedOverMetered(true)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                req.setDestinationInExternalFilesDir(
                                    context,
                                    Environment.DIRECTORY_DOWNLOADS,
                                    "edu-smart-${System.currentTimeMillis()}.pdf"
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                req.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    "edu-smart-${System.currentTimeMillis()}.pdf"
                                )
                            }
                            (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
                                .enqueue(req)
                            scope.launch { snackbar.showSnackbar("Download started.") }
                        }) { Icon(Icons.Default.Download, contentDescription = "Download") }
                    }

                    IconButton(onClick = {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            val bm = mapOf(
                                "url" to url,
                                "timestamp" to System.currentTimeMillis(),
                                "type" to titleType
                            )
                            FirebaseFirestore.getInstance()
                                .collection("users").document(user.uid)
                                .collection("bookmarks")
                                .add(bm)
                                .addOnSuccessListener {
                                    scope.launch { snackbar.showSnackbar("$displayTitle bookmarked.") }
                                }
                                .addOnFailureListener {
                                    scope.launch { snackbar.showSnackbar("Failed to bookmark.") }
                                }
                        } else {
                            scope.launch { snackbar.showSnackbar("You must be signed in.") }
                        }
                    }) { Icon(Icons.Default.Bookmark, contentDescription = "Bookmark") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (isLoading && customView == null) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            if (customView != null) {
                AndroidView(factory = { customView!! }, modifier = Modifier.fillMaxSize())
            } else {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.allowFileAccess = true
                            settings.allowContentAccess = true
                            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                            // Nicer reading experience
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false

                            webChromeClient = object : WebChromeClient() {
                                override fun onShowCustomView(
                                    view: View?,
                                    callback: CustomViewCallback?
                                ) {
                                    if (customView != null) {
                                        callback?.onCustomViewHidden()
                                        return
                                    }
                                    customView = view
                                    customViewCallback = callback
                                }

                                override fun onHideCustomView() {
                                    customView = null
                                    customViewCallback?.onCustomViewHidden()
                                    customViewCallback = null
                                }
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                }
                            }
                            setDownloadListener(DownloadListener { dlUrl, _, _, _, _ ->
                                try {
                                    val req = DownloadManager.Request(Uri.parse(dlUrl))
                                        .setTitle("Downloading…")
                                        .setNotificationVisibility(
                                            DownloadManager.Request
                                                .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                                        )
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        req.setDestinationInExternalFilesDir(
                                            ctx,
                                            Environment.DIRECTORY_DOWNLOADS,
                                            "edu-smart-${System.currentTimeMillis()}"
                                        )
                                    } else {
                                        @Suppress("DEPRECATION")
                                        req.setDestinationInExternalPublicDir(
                                            Environment.DIRECTORY_DOWNLOADS,
                                            "edu-smart-${System.currentTimeMillis()}"
                                        )
                                    }
                                    (ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
                                        .enqueue(req)
                                    scope.launch { snackbar.showSnackbar("Download started.") }
                                } catch (_: Throwable) {
                                    // ignore
                                }
                            })

                            loadUrl(finalUrl)
                            webViewRef = this
                        }
                    },
                    update = { wv ->
                        if (wv.url != finalUrl) {
                            isLoading = true
                            wv.loadUrl(finalUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    onRelease = {
                        it.stopLoading()
                        it.loadUrl("about:blank")
                        it.clearHistory()
                        it.removeAllViews()
                        it.webChromeClient = null
                        if (webViewRef === it) webViewRef = null
                        it.destroy()
                    }
                )
            }
        }
    }
}
