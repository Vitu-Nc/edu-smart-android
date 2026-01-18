package com.example.edu_smart

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.edu_smart.components.BottomBar
import com.example.edu_smart.navigation.NavGraph
import com.example.edu_smart.navigation.Screen
import com.example.edu_smart.ui.theme.EDUSMARTTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Show Splash as early as possible
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // 🔐 Ask for external-storage permission on Android 9 and below
        requestLegacyStoragePermissionIfNeeded()

        // Edge-to-edge for targetSdk 36 compliance
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            EDUSMARTTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                // Routes that should display the bottom bar
                val routesWithBottomBar = remember {
                    setOf(
                        Screen.Home.route,
                        Screen.Library.route,
                        Screen.Chatbot.route,
                        Screen.Tutorials.route,
                        Screen.BookList.route
                    )
                }

                val showBottomBar = remember(currentRoute) {
                    when {
                        currentRoute == null -> false
                        currentRoute in routesWithBottomBar -> true
                        // allow dynamic quiz routes like "quiz/{id}" or "quiz/123"
                        currentRoute.startsWith("quiz/") -> true
                        else -> false
                    }
                }

                Scaffold(
                    // Ensure content respects system bars (safe areas)
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        if (showBottomBar) {
                            BottomBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // 🔧 Only needed for Android 5–9 (API <= 28) where DownloadManager
    // writes directly to /storage/emulated/0/Download.
    private fun requestLegacyStoragePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val writeGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (!writeGranted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    100 // request code (any int)
                )
            }
        }
    }
}
