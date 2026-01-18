package com.example.edu_smart.model

data class Bookmark(
    val title: String = "",
    val workId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
