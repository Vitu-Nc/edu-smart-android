// model/LibraryDocument.kt
package com.example.edu_smart.model

data class LibraryDocument(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val url: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
