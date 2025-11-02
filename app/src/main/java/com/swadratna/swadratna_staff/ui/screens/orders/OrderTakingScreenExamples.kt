package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.navigation.NavigationRoute

/**
 * Examples showing different ways to navigate to OrderTakingScreen with customization options.
 */
object OrderTakingScreenExamples {

    /**
     * Example 1: Default behavior - shows both tabs with Menu as default
     */
    fun navigateToDefaultOrderTaking(navController: NavController, tableNumber: Int, orderId: String) {
        navController.navigate(NavigationRoute.OrderTaking.createRoute(
            tableNumber = tableNumber,
            orderId = orderId.toInt()
        ))
    }

    /**
     * Example 2: Show only Menu tab (hide Orders tab)
     */
    fun navigateToMenuOnly(navController: NavController, tableNumber: Int, orderId: String) {
        navController.navigate(NavigationRoute.OrderTaking.createRoute(
            tableNumber = tableNumber,
            orderId = orderId.toInt(),
            showMenuTab = true,
            showOrdersTab = false
        ))
    }

    /**
     * Example 3: Show only Orders tab (hide Menu tab)
     */
    fun navigateToOrdersOnly(navController: NavController, tableNumber: Int, orderId: String) {
        navController.navigate(NavigationRoute.OrderTaking.createRoute(
            tableNumber = tableNumber,
            orderId = orderId.toInt(),
            showMenuTab = false,
            showOrdersTab = true
        ))
    }

    /**
     * Example 4: Show both tabs but default to Orders tab instead of Menu
     */
    fun navigateToOrdersDefault(navController: NavController, tableNumber: Int, orderId: String) {
        navController.navigate(NavigationRoute.OrderTaking.createRoute(
            tableNumber = tableNumber,
            orderId = orderId.toInt(),
            showMenuTab = true,
            showOrdersTab = true,
            defaultTab = 1 // 1 for Orders tab, 0 for Menu tab
        ))
    }

    /**
     * Example 5: Hide both tabs - will show the default tab content without bottom navigation
     * When both showMenuTab and showOrdersTab are false, the content of the defaultTab will be displayed
     * but the bottom navigation bar will be hidden.
     */
    fun navigateToNoTabs(navController: NavController, tableNumber: Int, orderId: String) {
        navController.navigate(NavigationRoute.OrderTaking.createRoute(
            tableNumber = tableNumber,
            orderId = orderId.toInt(),
            showMenuTab = false,
            showOrdersTab = false
        ))
    }

    /**
     * Example 6: Kitchen staff view - only show Orders tab with Orders as default
     */
    fun navigateToKitchenView(navController: NavController, tableNumber: Int, orderId: String) {
        navController.navigate(NavigationRoute.OrderTaking.createRoute(
            tableNumber = tableNumber,
            orderId = orderId.toInt(),
            showMenuTab = false,
            showOrdersTab = true,
            defaultTab = 1
        ))
    }

    /**
     * Example 7: Waiter quick order entry - only show Menu tab
     */
    fun navigateToWaiterQuickOrder(navController: NavController, tableNumber: Int, orderId: String) {
        navController.navigate(NavigationRoute.OrderTaking.createRoute(
            tableNumber = tableNumber,
            orderId = orderId.toInt(),
            showMenuTab = true,
            showOrdersTab = false,
            defaultTab = 0
        ))
    }

    /**
     * Example 8: Show Menu content without bottom navigation bar
     * This will display the Menu tab content but hide the bottom navigation completely
     */
    fun navigateToMenuWithoutBottomBar(navController: NavController, tableNumber: Int, orderId: String) {
        navController.navigate(NavigationRoute.OrderTaking.createRoute(
            tableNumber = tableNumber,
            orderId = orderId.toInt(),
            showMenuTab = false,  // Hide bottom navigation
            showOrdersTab = false, // Hide bottom navigation
            defaultTab = 0 // Show Menu content
        ))
    }

    /**
     * Example 9: Show Orders content without bottom navigation bar
     * This will display the Orders tab content but hide the bottom navigation completely
     */
    fun navigateToOrdersWithoutBottomBar(navController: NavController, tableNumber: Int, orderId: String) {
        navController.navigate(NavigationRoute.OrderTaking.createRoute(
            tableNumber = tableNumber,
            orderId = orderId.toInt(),
            showMenuTab = false,  // Hide bottom navigation
            showOrdersTab = false, // Hide bottom navigation
            defaultTab = 1 // Show Orders content
        ))
    }
}