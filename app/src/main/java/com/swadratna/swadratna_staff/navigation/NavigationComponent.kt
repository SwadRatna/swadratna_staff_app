package com.swadratna.swadratna_staff.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.swadratna.swadratna_staff.ui.screens.home.HomeScreen

@Composable
fun NavigationComponent(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationRoute.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = NavigationRoute.Home.route) {
            HomeScreen()
        }
        
        composable(route = NavigationRoute.Profile.route) {
            // Add Profile screen when ready
        }
    }
}

sealed class NavigationRoute(val route: String) {
    object Home : NavigationRoute("home")
    object Profile : NavigationRoute("profile")
    // Add more routes as needed
}