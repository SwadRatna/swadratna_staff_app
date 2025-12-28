package com.swadratna.swadratna_staff.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import androidx.navigation.navDeepLink
import com.swadratna.swadratna_staff.MainActivity
import com.swadratna.swadratna_staff.ui.screens.inventory.InventoryScreen
import com.swadratna.swadratna_staff.ui.screens.orders.OrderTakingScreen
import com.swadratna.swadratna_staff.ui.screens.tables.TablesScreen
import com.swadratna.swadratna_staff.R
import com.swadratna.swadratna_staff.ui.orderDashboard.OrderDashboard
import com.swadratna.swadratna_staff.ui.screens.login.LoginScreen
import com.swadratna.swadratna_staff.ui.screens.orders.PayBillScreen
import com.swadratna.swadratna_staff.ui.screens.profile.StaffProfileScreen
import com.swadratna.swadratna_staff.ui.screens.kot.KotListScreen
import com.swadratna.swadratna_staff.ui.screens.sales.SalesReportScreen
import com.swadratna.swadratna_staff.ui.theme.fontFamily
import com.swadratna.swadratna_staff.ui.components.NotificationBottomSheet
import com.swadratna.swadratna_staff.ui.components.NotificationSheetData
import com.swadratna.swadratna_staff.ui.components.LocalNotificationManager

import androidx.compose.foundation.layout.statusBarsPadding

import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_staff.data.remote.model.StaffRole

import com.swadratna.swadratna_staff.ui.screens.orders.OrdersScreen
import com.swadratna.swadratna_staff.ui.screens.orders.OrderManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationComponent(
    navController: NavHostController = rememberNavController(),
    startDestination: String,
    deepLinkTrigger: Int = 0,
    viewModel: NavigationViewModel = hiltViewModel()
) {
    val staffRole by viewModel.staffRole.collectAsState()
    
    val items = remember(staffRole) {
        if (staffRole == StaffRole.MANAGER.roleName) {
            listOf(
                NavigationRoute.Orders,
                NavigationRoute.Tables,
                NavigationRoute.SalesReport
            )
        } else {
            listOf(
                NavigationRoute.Orders,
                NavigationRoute.Tables,
                NavigationRoute.KotList
            )
        }
    }

    // Get the current back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Find the NavigationRoute object based on the current destination
    val currentRoute = currentDestination?.route
    val currentScreen = items.find { it.route == currentRoute }
    val shouldShowBottomBar = items.any { it.route == currentRoute }

    // Handle deep link navigation
    val context = LocalContext.current
    LaunchedEffect(currentRoute, deepLinkTrigger) {
        // Only process deep links when we're on a valid screen (not login)
        if (currentRoute != NavigationRoute.Login.route && currentRoute != null) {
            Log.d("NavigationComponent", "Checking for deep link data on route: $currentRoute")
            val deepLinkData = MainActivity.getDeepLinkData(context)
            Log.d("NavigationComponent", "Deep link data found: ${deepLinkData != null}")
            
            if (deepLinkData != null) {
                val (type, orderId, billId, tableNumber) = deepLinkData
                Log.d("NavigationComponent", "Processing deep link - Type: $type, OrderId: $orderId, BillId: $billId, TableNumber: $tableNumber")
                
                if (type == "new_kot") {
                     val route = "orders/$orderId"
                     navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                    MainActivity.clearDeepLinkData(context)
                } else if (type == "approve_bill") {
                     val route = if (billId != null) {
                        NavigationRoute.Bill.createRoute(billId = billId)
                    } else {
                        NavigationRoute.Bill.route
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                    MainActivity.clearDeepLinkData(context)
                } else if (type == "order" || type == "deep_link_order" || type == "new_order") {
                    // Navigate to order taking screen
                    val route = "${NavigationRoute.OrderTaking.route}/$tableNumber/$orderId?showMenuTab=true&showOrdersTab=true&defaultTab=1"
                    Log.d("NavigationComponent", "Navigating to order taking screen: $route")
                    
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                    MainActivity.clearDeepLinkData(context)
                } else if (type == "payment_completed") {
                    // Navigate to bill screen
                    val route = "${NavigationRoute.Bill.route}?orderId=$orderId"
                    Log.d("NavigationComponent", "Navigating to bill screen: $route")
                    
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                    MainActivity.clearDeepLinkData(context)
                }
            }
        }
    }

    // Get notification manager and current notification
    val notificationManager = LocalNotificationManager.current
    val currentNotification by notificationManager.currentNotification.collectAsState()

    Scaffold(
        topBar = {
            if (shouldShowBottomBar) {
                TopAppBar(
                    title = {
                        Text(text = stringResource(R.string.company_name), fontFamily = FontFamily.Cursive, fontSize = 28.sp)
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
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
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
                InventoryScreen(navController = navController)
            }
            composable(route = NavigationRoute.Orders.route) {
                OrderDashboard(
                    Modifier.fillMaxSize(),
                    navController = navController
                )
            }
            composable(
                route = "orders/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId")
                val viewModel: OrderManagementViewModel = hiltViewModel()
                OrdersScreen(
                    navController = navController,
                    orderID = orderId,
                    viewModel = viewModel
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
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "swadratna://order?tableNumber={tableNumber}&orderId={orderId}"
                    },
                    navDeepLink {
                        uriPattern = "https://swadratna.com/order?tableNumber={tableNumber}&orderId={orderId}"
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
                route = "${NavigationRoute.Bill.route}?orderId={orderId}&billId={billId}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType; nullable = true },
                    navArgument("billId") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId")
                val billId = backStackEntry.arguments?.getString("billId")
                PayBillScreen(navController = navController, orderId = orderId, billId = billId)
            }
            composable(route = NavigationRoute.KotList.route) {
                KotListScreen(navController = navController)
            }
            composable(route = NavigationRoute.SalesReport.route) {
                SalesReportScreen(navController = navController)
            }
            composable(route = NavigationRoute.TableManager.route) {
                com.swadratna.swadratna_staff.ui.screens.tables.TableManagerScreen(navController = navController)
            }
        }
        
            // Show in-app notification sheet if there's a current notification
            currentNotification?.let { notification ->
                val sheetData = NotificationSheetData(
                    title = notification.title,
                    body = notification.message,
                    type = notification.type,
                    orderId = notification.orderId?.toString(),
                    billId = notification.billId,
                    tableNumber = notification.tableNumber?.toString(),
                    deepLink = notification.deepLink
                )

                NotificationBottomSheet(
                    data = sheetData,
                    onDismiss = {
                        notificationManager.dismissNotification()
                    },
                    onViewClick = {
                        notificationManager.dismissNotification()
                        
                        when (notification.type) {
                            "new_kot" -> {
                                val orderId = notification.orderId?.toString()
                                if (orderId != null) {
                                     navController.navigate("orders/$orderId") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else {
                                    navController.navigate(NavigationRoute.Orders.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            "approve_bill" -> {
                                val billId = notification.billId
                                val route = if (billId != null) {
                                    NavigationRoute.Bill.createRoute(billId = billId)
                                } else {
                                    NavigationRoute.Bill.route
                                }
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            else -> {
                                // Fallback to existing deep link logic
                                notification.deepLink?.let { deepLink ->
                                    try {
                                        val uri = android.net.Uri.parse(deepLink)
                                        val tableNumber = uri.getQueryParameter("tableNumber")?.toIntOrNull()
                                        val orderId = uri.getQueryParameter("orderId")?.toIntOrNull()
                                        
                                        if (tableNumber != null && orderId != null) {
                                            val route = "${NavigationRoute.OrderTaking.route}/$tableNumber/$orderId?showMenuTab=true&showOrdersTab=true&defaultTab=1"
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            navController.navigate(deepLink) {
                                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("NavigationComponent", "Error parsing deep link: $deepLink", e)
                                        // Fallback manual construction
                                        if (notification.type == "new_order" || notification.type == "payment_completed") {
                                            notification.orderId?.let { orderId ->
                                                notification.tableNumber?.let { tableNumber ->
                                                    val route = "${NavigationRoute.OrderTaking.route}/$tableNumber/$orderId?showMenuTab=true&showOrdersTab=true&defaultTab=1"
                                                    navController.navigate(route) {
                                                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                                        launchSingleTop = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } ?: run {
                                     // Manual construction fallback if no deep link
                                     if (notification.type == "new_order" || notification.type == "payment_completed") {
                                         notification.orderId?.let { orderId ->
                                             notification.tableNumber?.let { tableNumber ->
                                                 val route = "${NavigationRoute.OrderTaking.route}/$tableNumber/$orderId?showMenuTab=true&showOrdersTab=true&defaultTab=1"
                                                 navController.navigate(route) {
                                                     popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                                     launchSingleTop = true
                                                 }
                                             }
                                         }
                                     }
                                }
                            }
                        }
                    }
                )
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
        fun createRoute(orderId: String? = null, billId: String? = null): String {
            return if (orderId != null) {
                "bill?orderId=$orderId"
            } else {
                "bill?billId=$billId"
            }
        }
    }
    object KotList : NavigationRoute("kot_list", "KOTs", R.drawable.ic_kot)
    object SalesReport : NavigationRoute("sales_report", "Sales", R.drawable.ic_recipt)
    object TableManager : NavigationRoute("table_manager", "Table Manager", R.drawable.ic_launcher_foreground) // Placeholder icon
}