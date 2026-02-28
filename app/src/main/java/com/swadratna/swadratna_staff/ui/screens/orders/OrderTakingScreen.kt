package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.components.NavButton
import com.swadratna.swadratna_staff.ui.theme.RedGrey20
import com.swadratna.swadratna_staff.ui.components.NetworkTopSnackbarHost
import kotlin.math.absoluteValue

import com.swadratna.swadratna_staff.data.remote.model.StaffRole

/**
 * Order taking screen with customizable bottom navigation options.
 *
 * @param navController Navigation controller for screen transitions
 * @param tableNumber The table number for the current order
 * @param orderId Optional order ID for existing orders
 * @param showMenuTab Whether to show the Menu tab in the bottom navigation (default: true)
 * @param showOrdersTab Whether to show the Orders tab in the bottom navigation (default: true)
 * @param defaultTab The default tab to show when the screen loads (0 for Menu, 1 for Orders)
 *         Note: When both showMenuTab and showOrdersTab are false, the content of the defaultTab
 *         will still be displayed, but without the bottom navigation bar.
 * @param viewModel The ViewModel for managing order data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTakingScreen(
    navController: NavController,
    tableNumber: Int,
    orderId: Int? = null,
    showMenuTab: Boolean = true,
    showOrdersTab: Boolean = true,
    defaultTab: Int = 0,
    onBack: () -> Unit,
    viewModel: OrderManagementViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf(defaultTab.coerceIn(0, 1)) }
    val role by viewModel.staffRole.collectAsState()

    // Free table dialog state
    var showFreeTableDialog by remember { mutableStateOf(false) }
    val freeTableState by viewModel.freeTableState.collectAsState()
    val cancelOrderState by viewModel.cancelOrderState.collectAsState()
    val tableListState by viewModel.tableListState.collectAsState()

    val tableStringId = remember(tableListState, tableNumber) {
        if (tableNumber == 0) {
            "Parcel"
        } else if (tableListState is TableListState.Success) {
            val tables = (tableListState as TableListState.Success).tables.tables
            tables.find { it.id == tableNumber }?.table_id ?: tableNumber.toString()
        } else {
            tableNumber.toString()
        }
    }

    // Validate tab selection based on available tabs
    LaunchedEffect(showMenuTab, showOrdersTab) {
        when {
            !showMenuTab && !showOrdersTab -> {
                // No tabs available, but still show the default tab content
                selectedTab = defaultTab.coerceIn(0, 1)
            }
            !showMenuTab && showOrdersTab -> selectedTab = 1 // Only orders tab available
            showMenuTab && !showOrdersTab -> selectedTab = 0 // Only menu tab available
            else -> selectedTab = defaultTab.coerceIn(0, 1) // Both tabs available, use default
        }
    }

    LaunchedEffect(categories) {
        if (categories.isNotEmpty()) {
            if (selectedCategoryId == null || categories.none { it.id == selectedCategoryId }) {
                selectedCategoryId = categories.first().id
            }
        }
    }

    // Handle free table response
    LaunchedEffect(freeTableState) {
        when (freeTableState) {
            is FreeTableState.Success -> {
                viewModel.resetFreeTableState()
                onBack() // Navigate back to table screen
            }
            is FreeTableState.Error -> {
                // Error is handled by the ViewModel, just reset state
                viewModel.resetFreeTableState()
            }
            else -> {} // Do nothing for Loading or Idle states
        }
    }

    LaunchedEffect(cancelOrderState) {
        when (cancelOrderState) {
            is CancelOrderState.Success -> {
                viewModel.resetCancelOrderState()
                onBack()
            }
            is CancelOrderState.Error -> {
                viewModel.resetCancelOrderState()
            }
            else -> {}
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (tableNumber == 0) "Parcel" else "Table $tableStringId",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Order ID: $orderId",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (role == StaffRole.MANAGER.roleName) {
                        Button(
                            onClick = { showFreeTableDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary,
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(if (tableNumber == 0) "Cancel Parcel" else "Free Table",fontSize = 14.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            if (showMenuTab || showOrdersTab) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface).padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Button 1: Menu
                    if (showMenuTab) {
                        NavButton(
                            label = "Menu",
                            icon = Icons.Filled.Menu,
                            isSelected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                      },
                            unselectedContainerColor = RedGrey20,
                            modifier = Modifier.weight(if (showOrdersTab) 1f else 2f)
                        )
                    }

                    // Button 2: Orders
                    if (showOrdersTab) {
                        NavButton(
                            label = "Orders",
                            icon = Icons.Filled.AccountBox,
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            unselectedContainerColor = RedGrey20,
                            modifier = Modifier.weight(if (showMenuTab) 1f else 2f)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (selectedTab) {
            0 -> OrderMenuScreen(
                modifier = Modifier.padding(paddingValues),
                tableNumber = tableNumber,
                orderId = orderId.toString(),
                navController = navController
            )
            1 -> OrdersScreen(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                orderID = orderId.toString(),
                viewModel = viewModel
            )
        }
    }

    // Free Table Confirmation Dialog
    if (showFreeTableDialog) {
        val isParcel = tableNumber == 0
        AlertDialog(
            onDismissRequest = { showFreeTableDialog = false },
            title = { Text(if (isParcel) "Cancel Parcel" else "Free Table") },
            text = {
                Text(
                    if (isParcel) "Are you sure you want to cancel this parcel order?"
                    else "Are you sure you want to free table $tableStringId? This action will cancel the current order."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFreeTableDialog = false
                        if (isParcel && orderId != null) {
                            viewModel.cancelOrder(orderId, "User cancelled")
                        } else {
                            viewModel.freeTheTable(
                                tableNumber,
                                true,
                                "Table freed by staff"
                            )
                        }
                    }
                ) {
                    Text(if (isParcel) "Yes, Cancel Parcel" else "Yes, Free Table")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFreeTableDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
