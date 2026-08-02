package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ActivityLogViewModel

@Composable
fun KetayPredictorApp(modifier: Modifier = Modifier, activityLogViewModel: ActivityLogViewModel) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                modifier = Modifier,
                activityLogViewModel = activityLogViewModel,
                onNavigateToLogs = { navController.navigate("logs") },
                onNavigateToDeveloper = { navController.navigate("developer") }
            )
        }
        composable("logs") {
            ActivityLogScreen(
                viewModel = activityLogViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("developer") {
            DeveloperScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
