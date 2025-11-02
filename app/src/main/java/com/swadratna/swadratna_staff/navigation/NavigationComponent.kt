package com.swadratna.swadratna_staff.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
import com.swadratna.swadratna_staff.ui.screens.tables.TablesScreen
import com.swadratna.swadratna_staff.R
import com.swadratna.swadratna_staff.ui.orderDashboard.OrderDashboard
import com.swadratna.swadratna_staff.ui.screens.login.LoginScreen
import com.swadratna.swadratna_staff.ui.screens.orders.PayBillScreen
import com.swadratna.swadratna_staff.ui.screens.profile.StaffProfileScreen
import com.swadratna.swadratna_staff.ui.screens.kot.KotListScreen
import com.swadratna.swadratna_staff.ui.theme.fontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationComponent(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    val items = listOf(
        NavigationRoute.Orders,
        NavigationRoute.KotList,
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
                        Text(text = stringResource(R.string.company_name), fontFamily = FontFamily.Cursive,
                            fontWeight =  FontWeight.Bold, fontSize = 28.sp)
                    }, actions = {
                        IconButton(
                            onClick = {
                            navController.navigate(NavigationRoute.Profile.route)
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_person),
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
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(painter = painterResource( screen.icon), contentDescription = null) },
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
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = Color.Gray,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        NavHost(
            modifier = Modifier.
            consumeWindowInsets(innerPadding)
                .padding(innerPadding),
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
            composable(route = NavigationRoute.Orders.route) {
                OrderDashboard(
                    Modifier.fillMaxSize(),
                    navController = navController
                )
            }
            composable(route = NavigationRoute.Profile.route) {
                StaffProfileScreen(navController = navController)
            }
            composable(
                route = "${NavigationRoute.OrderTaking.route}/{tableNumber}/{orderId}?showMenuTab={showMenuTab}&showOrdersTab={showOrdersTab}&defaultTab={defaultTab}",
                arguments = listOf(
                    navArgument("tableNumber") { type = NavType.IntType },
                    navArgument("orderId") { type = NavType.StringType },
                    navArgument("showMenuTab") { 
                        type = NavType.BoolType
                        defaultValue = true 
                    },
                    navArgument("showOrdersTab") { 
                        type = NavType.BoolType
                        defaultValue = true 
                    },
                    navArgument("defaultTab") { 
                        type = NavType.IntType
                        defaultValue = 0 
                    }
                )
            ) { backStackEntry ->
                val tableNumber = backStackEntry.arguments?.getInt("tableNumber") ?: 0
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val showMenuTab = backStackEntry.arguments?.getBoolean("showMenuTab") ?: true
                val showOrdersTab = backStackEntry.arguments?.getBoolean("showOrdersTab") ?: true
                val defaultTab = backStackEntry.arguments?.getInt("defaultTab") ?: 0
                
                OrderTakingScreen(
                    navController = navController,
                    tableNumber = tableNumber,
                    orderId = orderId.toIntOrNull(),
                    showMenuTab = showMenuTab,
                    showOrdersTab = showOrdersTab,
                    defaultTab = defaultTab,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "${NavigationRoute.Bill.route}/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId")
                PayBillScreen(navController = navController, orderId = orderId)
            }
            composable(route = NavigationRoute.KotList.route) {
                KotListScreen(navController = navController)
            }
        }
    }
}

sealed class NavigationRoute(val route: String, val title: String, val icon: Int) {
    object Login : NavigationRoute("login", "Login", R.drawable.ic_login)
    object Orders : NavigationRoute("orders", "Orders",R.drawable.ic_order) {
        fun createRoute(orderId: String) = "orders/$orderId"
    }
    object Tables : NavigationRoute("tables", "Tables", R.drawable.ic_table)
    object Inventory : NavigationRoute("inventory", "Inventory", R.drawable.ic_inventory)
    object Profile : NavigationRoute("profile", "Profile", R.drawable.ic_person)
    object OrderTaking : NavigationRoute("order_taking", "Order Taking", R.drawable.ic_order) {
        fun createRoute(
            tableNumber: Int, 
            orderId: Int,
            showMenuTab: Boolean = true,
            showOrdersTab: Boolean = true,
            defaultTab: Int = 0
        ): String {
            val baseRoute = "${route}/$tableNumber/$orderId"
            val params = mutableListOf<String>()
            
            if (!showMenuTab) params.add("showMenuTab=false")
            if (!showOrdersTab) params.add("showOrdersTab=false")
            if (defaultTab != 0) params.add("defaultTab=$defaultTab")
            
            return if (params.isEmpty()) baseRoute else "$baseRoute?${params.joinToString("&")}"
        }
    }

//    object AllOrderScreen : NavigationRoute("order_taking", "Order Taking", Icons.Default.List)
//
//    object OrderMenuScreen : NavigationRoute("order_taking", "Order Taking", Icons.Default.List)

    object Bill : NavigationRoute("bill", "Bill", R.drawable.ic_recipt) {
        fun createRoute(orderId: String) = "bill/$orderId"
    }
    object KotList : NavigationRoute("kot_list", "KOTs", R.drawable.ic_kot)
}