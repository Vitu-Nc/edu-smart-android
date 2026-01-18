package com.example.edu_smart.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.edu_smart.model.VideoItem
import com.example.edu_smart.models.Subject
import com.example.edu_smart.screens.*
import com.example.edu_smart.utils.openYouTubeVideo
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val startDestination =
        if (FirebaseAuth.getInstance().currentUser != null)
            Screen.Home.route
        else
            Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 🔐 Auth
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Signup.route) { SignupScreen(navController) }

        // 🏠 Home
        composable(Screen.Home.route) { HomeScreen(navController) }

        // 📚 Library
        composable(Screen.Library.route) { LibraryScreen(navController) }

        // 🌐 WebView (books, news, docs, etc.)
        composable(
            route = "${Screen.WebView.route}/{url}/{type}",
            arguments = listOf(
                navArgument("url")  { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val type       = backStackEntry.arguments?.getString("type") ?: "Content"
            val decodedUrl  = Uri.decode(encodedUrl)
            val decodedType = Uri.decode(type)
            WebViewScreen(
                navController = navController,
                url = decodedUrl,
                titleType = decodedType
            )
        }

        // 📰 News
        composable(Screen.News.route) { NewsScreen(navController) }

        // 🤖 Chatbot
        composable(Screen.Chatbot.route) { ChatbotScreen() }

        // 🎓 Tutorials — open native YouTube app on click
        composable(Screen.Tutorials.route) {
            val context = LocalContext.current
            TutorialsScreen(
                onVideoClick = { video: VideoItem ->
                    context.openYouTubeVideo(video.videoId)
                }
            )
        }

        // 🎥 Video detail
        composable(
            route = "${Screen.VideoDetail.route}/{videoId}/{title}/{description}",
            arguments = listOf(
                navArgument("videoId")     { type = NavType.StringType },
                navArgument("title")       { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val videoId     = backStackEntry.arguments?.getString("videoId") ?: ""
            val title       = Uri.decode(backStackEntry.arguments?.getString("title") ?: "")
            val description = Uri.decode(backStackEntry.arguments?.getString("description") ?: "")
            VideoDetailScreen(
                videoId = videoId,
                title = title,
                description = description
            )
        }

        // 🧩 Subject picker (entry to quiz module)
        composable(Screen.SubjectPicker.route) {
            SubjectPickerScreen { selectedSubject ->
                navController.navigate(Screen.Quiz.withArgs(selectedSubject))
            }
        }

        // ❓ Quiz screen
        composable(
            route = "${Screen.Quiz.route}/{subject}",
            arguments = listOf(navArgument("subject") { type = NavType.StringType })
        ) { backStackEntry ->
            val subjectArg = backStackEntry.arguments?.getString("subject").orEmpty()
            val subject = runCatching { Subject.valueOf(subjectArg) }
                .getOrElse { Subject.RANDOM }

            QuizScreen(
                subject = subject,
                onExit = { navController.popBackStack() }
            )
        }
    }
}
