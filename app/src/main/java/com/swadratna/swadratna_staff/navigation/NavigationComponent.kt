package com.swadratna.swadratna_staff.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.swadratna.swadratna_staff.ui.screens.inventory.InventoryScreen
import com.swadratna.swadratna_staff.ui.screens.orders.OrderTakingScreen
import com.swadratna.swadratna_staff.ui.screens.orders.OrdersScreen
import com.swadratna.swadratna_staff.ui.screens.tables.TablesScreen
import com.swadratna.swadratna_staff.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationComponent(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationRoute.Tables.route
) {
    val items = listOf(
        NavigationRoute.Orders,
        NavigationRoute.Tables,
        NavigationRoute.Inventory
    )

    // Get the current back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Find the NavigationRoute object based on the current destination
    val currentRoute = currentDestination?.route
    val currentScreen = items.find { it.route == currentRoute }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = currentScreen?.title ?: stringResource(R.string.app_name))
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = startDestination
        ) {
            composable(route = NavigationRoute.Orders.route) {
                OrdersScreen()
            }
            composable(route = NavigationRoute.Tables.route) {
                TablesScreen(navController)
            }
            composable(route = NavigationRoute.Inventory.route) {
                InventoryScreen()
            }
            composable(route = NavigationRoute.Profile.route) {
                // Add Profile screen when ready
            }
            composable(
                route = "${NavigationRoute.OrderTaking.route}/{tableNumber}/{customerName}",
                arguments = listOf(
                    navArgument("tableNumber") { type = NavType.IntType },
                    navArgument("customerName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tableNumber = backStackEntry.arguments?.getInt("tableNumber") ?: 0
                val customerName = backStackEntry.arguments?.getString("customerName") ?: ""
                OrderTakingScreen(
                    tableNumber = tableNumber,
                    customerName = customerName,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

sealed class NavigationRoute(val route: String, val title: String, val icon: ImageVector) {
    object Orders : NavigationRoute("orders", "Orders", Icons.Default.List)
    object Tables : NavigationRoute("tables", "Tables", Icons.Default.Home)
    object Inventory : NavigationRoute("inventory", "Inventory", Icons.Default.ShoppingCart)
    object Profile : NavigationRoute("profile", "Profile", Icons.Default.Person)
    object OrderTaking : NavigationRoute("order_taking", "Order Taking", Icons.Default.List)
}