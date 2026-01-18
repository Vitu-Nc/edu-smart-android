package com.example.edu_smart.navigation

import android.net.Uri
import com.example.edu_smart.models.Subject

/**
 * Central navigation graph definition.
 * Every screen in the app has a unique route managed here.
 */
sealed class Screen(val route: String) {

    // 🔐 Authentication Screens
    object Login : Screen("login")
    object Signup : Screen("signup")

    // 🏠 Main Sections (Bottom Nav)
    object Home : Screen("home")
    object Library : Screen("library")
    object News : Screen("news")
    object Chatbot : Screen("chatbot")
    object Tutorials : Screen("tutorials")
    object Upload : Screen("upload")

    // 🧩 Subject Picker (entry point for the Quiz module)
    object SubjectPicker : Screen("subjects")

    // 📚 Library Module
    object BookList : Screen("book_list")

    object BookDetail : Screen("book_detail") {
        fun withArgs(workId: String): String = "$route/$workId"
    }

    // ❓ Quiz Module (Subject-based)
    object Quiz : Screen("quiz") {

        /** Pass Subject enum directly */
        fun withArgs(subject: Subject): String =
            "$route/${subject.name}"

        /** Pass raw string – must exactly match Subject enum name */
        fun withArgsRaw(subjectName: String): String =
            "$route/$subjectName"
    }

    // 🌐 WebView Module for online content
    object WebView : Screen("webview") {

        fun withArgs(url: String, type: String = "Content"): String {
            val encodedUrl = Uri.encode(url)
            val encodedType = Uri.encode(type)
            return "$route/$encodedUrl/$encodedType"
        }
    }

    // 📄 PDF Viewer
    object PdfViewer : Screen("pdfviewer") {

        fun withArgs(url: String): String {
            val encodedUrl = Uri.encode(url)
            return "$route/$encodedUrl"
        }
    }

    // 🎥 Video Detail Screen (Tutorials)
    object VideoDetail : Screen("video_detail") {

        fun withArgs(videoId: String, title: String, description: String): String {
            val encodedTitle = Uri.encode(title)
            val encodedDescription = Uri.encode(description)
            return "$route/$videoId/$encodedTitle/$encodedDescription"
        }
    }

    // ▶️ Optional YouTube player (if you ever use embedded playback)
    object YouTubePlayer : Screen("ytplayer") {
        fun withArgs(videoId: String): String = "$route/$videoId"
    }
}