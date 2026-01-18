package com.example.edu_smart.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home),
    BottomNavItem("Library", Screen.Library.route, Icons.Filled.Book),
    BottomNavItem("News", Screen.News.route, Icons.Filled.Newspaper),
    BottomNavItem("Chatbot", Screen.Chatbot.route, Icons.Filled.Chat),
    BottomNavItem("Quiz", Screen.SubjectPicker.route, Icons.Filled.School),
    BottomNavItem("Tutorials", Screen.Tutorials.route, Icons.Filled.School)
)
