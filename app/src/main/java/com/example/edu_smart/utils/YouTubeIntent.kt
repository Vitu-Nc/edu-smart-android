package com.example.edu_smart.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens a YouTube video in the native app if available; falls back to browser.
 * @param videoId the pure ID, e.g. "dQw4w9WgXcQ"
 * @param startSeconds optional start time in seconds
 */
fun Context.openYouTubeVideo(videoId: String, startSeconds: Int? = null) {
    val appIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("vnd.youtube:$videoId")
    ).apply {
        setPackage("com.google.android.youtube")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val webUri = "https://www.youtube.com/watch?v=$videoId" +
            (startSeconds?.let { "&t=${it}s" } ?: "")
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUri)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        startActivity(appIntent)
    } catch (_: ActivityNotFoundException) {
        startActivity(webIntent)
    }
}
