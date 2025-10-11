package com.swadratna.swadratna_staff.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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


import com.swadratna.swadratna_staff.ui.screens.login.LoginScreen
import com.swadratna.swadratna_staff.ui.screens.orders.PayBillScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationComponent(
    navController: NavHostController = rememberNavController(),
    startDestination: String
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
    val shouldShowBottomBar = items.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            if (shouldShowBottomBar) {
                TopAppBar(
                    title = {
                        Text(text = stringResource(R.string.company_name))
                    }, actions = {
                        IconButton(
                            onClick = { /* Handle profile click */ },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.surface
                            )
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(0.6f),
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        bottomBar = {
            if (shouldShowBottomBar) {
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
                            })
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = startDestination
        ) {
            composable(route = NavigationRoute.Login.route) {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(NavigationRoute.Tables.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                })
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
                route = "${NavigationRoute.OrderTaking.route}/{tableNumber}/{orderId}",
                arguments = listOf(
                    navArgument("tableNumber") { type = NavType.IntType },
                    navArgument("orderId") { type = NavType.StringType })) { backStackEntry ->
                val tableNumber = backStackEntry.arguments?.getInt("tableNumber") ?: 0
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderTakingScreen(
                    tableNumber = tableNumber,
                    orderId = orderId,
                    onBack = { navController.popBackStack() },
                    navController = navController)
            }
            composable(
                route = "${NavigationRoute.Bill.route}/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId")
                PayBillScreen(navController = navController, orderId = orderId)
            }
        }
    }
}

sealed class NavigationRoute(val route: String, val title: String, val icon: ImageVector) {
    object Login : NavigationRoute("login", "Login", Icons.Default.Person)
    object Orders : NavigationRoute("orders", "Orders", Icons.Default.List) {
        fun createRoute(orderId: String) = "orders/$orderId"
    }
    object Tables : NavigationRoute("tables", "Tables", Icons.Default.Home)
    object Inventory : NavigationRoute("inventory", "Inventory", Icons.Default.ShoppingCart)
    object Profile : NavigationRoute("profile", "Profile", Icons.Default.Person)
    object OrderTaking : NavigationRoute("order_taking", "Order Taking", Icons.Default.List)

    object AllOrderScreen : NavigationRoute("order_taking", "Order Taking", Icons.Default.List)

    object OrderMenuScreen : NavigationRoute("order_taking", "Order Taking", Icons.Default.List)

    object Bill : NavigationRoute("bill", "Bill", Icons.Default.List) {
        fun createRoute(orderId: String) = "bill/$orderId"
    }
}