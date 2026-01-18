package com.example.edu_smart.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.edu_smart.navigation.bottomNavItems

@Composable
fun BottomBar(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    // Only navigate if we're not already on this route
                    if (!selected) {
                        navController.navigate(item.route) {
                            // Pop up to the graph's start destination to avoid a giant backstack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a tab
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.name) },
                label = { Text(item.name) }
            )
        }
    }
}
