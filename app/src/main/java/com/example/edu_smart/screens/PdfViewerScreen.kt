package com.example.edu_smart.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

// Normalise Firebase Storage URLs so they download/open properly
private fun normalizeFirebasePdfUrl(raw: String): String {
    val uri = Uri.parse(raw)
    return if (uri.host?.contains("firebasestorage.googleapis.com") == true) {
        if (uri.getQueryParameter("alt") != "media") {
            uri.buildUpon()
                .appendQueryParameter("alt", "media")
                .build()
                .toString()
        } else raw
    } else raw
}

@Composable
fun PdfViewerScreen(
    navController: NavController,
    pdfUrl: String   // already decoded in NavGraph
) {
    val context = LocalContext.current

    LaunchedEffect(pdfUrl) {
        val fixedUrl = normalizeFirebasePdfUrl(pdfUrl)

        try {
            val uri = Uri.parse(fixedUrl)

            // 1) Try open as a real PDF
            val pdfIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            try {
                context.startActivity(
                    Intent.createChooser(pdfIntent, "Open PDF with")
                )
            } catch (e: ActivityNotFoundException) {
                // 2) No dedicated PDF app → fallback to browser
                val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(browserIntent)
                } catch (e2: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "No app can open this file. Please install a browser or PDF reader.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening file.", Toast.LENGTH_LONG).show()
        } finally {
            // Go back to the Library screen after launching the external app
            navController.popBackStack()
        }
    }
}
