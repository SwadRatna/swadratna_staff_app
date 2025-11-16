package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.swadratna.swadratna_staff.data.remote.model.Category
import com.swadratna.swadratna_staff.data.remote.model.MenuItem
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.components.CategoryItem
import com.swadratna.swadratna_staff.ui.components.SearchBar
import com.swadratna.swadratna_staff.ui.components.SlideToConfirm
import com.swadratna.swadratna_staff.ui.theme.Red80
import com.swadratna.swadratna_staff.utils.BillPrinterUtil
import kotlin.collections.component1
import kotlin.collections.component2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderMenuScreen(
    modifier: Modifier = Modifier,
    tableNumber: Int,
    orderId: String,
    navController: NavController,
    viewModel: OrderManagementViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val menuItemsMap by viewModel.menuItemsMap.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentOrderItems by viewModel.currentOrderItems.collectAsState()
    val orderConfirmationState by viewModel.orderConfirmationState.collectAsState()

    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showOrderConfirmationDialog by remember { mutableStateOf(false) }
    var showExpandedOrderSummary by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(0) } // 0 for Menu, 1 for Orders
    val staffLocationId by viewModel.staffLocationId.collectAsState()

    LaunchedEffect(staffLocationId) {
        staffLocationId?.let {staffLocationId ->
            viewModel.getMenuItems(staffLocationId, "")
        }
    }

    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id // Use category.id
        }
    }

    val currentMenuItems: List<MenuItem> = remember(selectedCategoryId, menuItemsMap) {
        selectedCategoryId?.let { id ->
            menuItemsMap[id.toString()]
        } ?: emptyList()
    }

    val filteredMenuItems = remember(currentMenuItems, searchQuery) {
        currentMenuItems.filter { item ->
            item.isAvailable && item.name.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalItemsInOrder = currentOrderItems.values.sum()

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }
            } else {
                SearchBar(
                    hintText = "Search Menu Items...",
                    query = searchQuery,
                    onQueryChanged = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CategoryList(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId, // Pass ID
                        onCategorySelected = { selectedCategoryId = it?.id }, // Use category.id
                        modifier = Modifier.weight(0.35f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    MenuItemList(
                        menuItems = filteredMenuItems,
                        orderItems = currentOrderItems,
                        onUpdateOrder = { itemId, quantity ->
                            viewModel.updateOrderItem(itemId, quantity)
                        },
                        modifier = Modifier.weight(0.65f)
                    )
                }
            }
        }

        if (showOrderConfirmationDialog) {
            OrderConfirmationDialog(
                orderId = orderId,
                tableNumber = tableNumber,
                currentOrderItems = currentOrderItems,
                menuItemsMap = menuItemsMap,
                orderConfirmationState = orderConfirmationState,
                onConfirmOrder = { viewModel.confirmOrder(orderId.toInt()) },
                onDismiss = {
                    showOrderConfirmationDialog = false; viewModel.resetOrderConfirmationState()
                },
                onResetState = { viewModel.resetOrderConfirmationState() }
            )
        }
        
        if (showExpandedOrderSummary) {
            OrderSummaryDialog(
                currentOrderItems = currentOrderItems,
                menuItems = menuItemsMap.values.flatten(), // Use all available menu items instead of filtered ones
                onDismiss = { showExpandedOrderSummary = false },
                onOrderClick = { 
                    showExpandedOrderSummary = false
                    showOrderConfirmationDialog = true 
                },
                onQuantityChange = { itemId, quantity ->
                    viewModel.updateOrderItem(itemId, quantity)
                },
                onRemoveItem = { itemId ->
                    viewModel.updateOrderItem(itemId, 0)
                }
            )
        }
        if (totalItemsInOrder > 0) {
            CompactOrderSummaryPill(
                totalItems = totalItemsInOrder,
                totalPrice = currentOrderItems.entries.sumOf {
                    val menuItem = menuItemsMap.values.flatten().find { item -> item.id == it.key }
                    (menuItem?.price ?: 0.0).toDouble() * it.value
                },
                onPillClick = { showExpandedOrderSummary = true },
                onOrderClick = { showOrderConfirmationDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun CategoryList(
    categories: List<Category>,
    selectedCategoryId: Int?, // Changed type to Int?
    onCategorySelected: (Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(categories) { category ->
            // Check selection using ID
            CategoryItem(
                category = category,
                isSelected = category.id == selectedCategoryId,
                onCategorySelected = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun MenuItemList(
    menuItems: List<MenuItem>,
    orderItems: Map<Int, Int>,
    onUpdateOrder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(menuItems) { item ->
            MenuItemOrderCard(
                item = item,
                quantity = orderItems.getOrDefault(item.id, 0),
                onUpdateOrder = { newQuantity ->
                    onUpdateOrder(item.id, newQuantity)
                }
            )
        }
    }
}

@Composable
fun MenuItemOrderCard(
    item: MenuItem,
    quantity: Int,
    onUpdateOrder: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Item Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${item.price}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quantity Controls
            if (quantity == 0) {
                Button(
                    onClick = { onUpdateOrder(1) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.widthIn(min = 80.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                CircleShape
                            )
                            .clickable(
                                enabled = true,
                                onClick = {
                                    onUpdateOrder(quantity - 1)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = quantity.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.widthIn(min = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .clickable(
                                enabled = true,
                                onClick = {
                                    onUpdateOrder(quantity + 1)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSummaryDialog(
    currentOrderItems: Map<Int, Int>,
    menuItems: List<MenuItem>,
    onDismiss: () -> Unit,
    onOrderClick: () -> Unit,
    onQuantityChange: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        title = { Text("Order Summary") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentOrderItems.toList()) { (itemId, quantity) ->
                    val menuItem = menuItems.find { it.id == itemId }
                    if (menuItem != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = menuItem.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "₹${menuItem.price}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { onQuantityChange(itemId, quantity - 1) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Decrease",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = quantity.toString(),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.widthIn(min = 24.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(
                                        onClick = { onQuantityChange(itemId, quantity + 1) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onRemoveItem(itemId) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOrderClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Place Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactOrderSummaryPill(
    totalItems: Int,
    totalPrice: Double,
    onPillClick: () -> Unit,
    onOrderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Shopping cart icon with badge
            Box {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Cart",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                if (totalItems > 0) {
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                    ) {
                        Text(
                            text = totalItems.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Price info
            Column {
                Text(
                    text = "₹%.2f".format(totalPrice),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$totalItems items",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            // Expand button
            IconButton(
                onClick = onPillClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Order button
            Button(
                onClick = onOrderClick,
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Order",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSummaryCard(
    currentOrderItems: Map<Int, Int>,
    menuItemsMap: Map<String, List<MenuItem>>,
    onUpdateOrder: (Int, Int) -> Unit
) {
    val allMenuItems =
        remember(menuItemsMap) { menuItemsMap.values.flatten().associateBy { it.id } }
    val totalItems = currentOrderItems.values.sum()
    val totalPrice = currentOrderItems.entries.sumOf {
        val menuItem = allMenuItems[it.key]
        (menuItem?.price ?: 0.0).toDouble() * it.value
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order Summary ($totalItems items)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "₹%.2f".format(totalPrice.toDouble()),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentOrderItems.entries.toList()) { (itemId, quantity) ->
                    val menuItem = allMenuItems[itemId]
                    menuItem?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(it.name, fontWeight = FontWeight.Medium)
                                Text(
                                    "₹%.2f".format(it.price.toDouble()),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onUpdateOrder(itemId, quantity - 1) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Decrease quantity"
                                    )
                                }
                                Text("$quantity", fontWeight = FontWeight.Bold)
                                IconButton(onClick = { onUpdateOrder(itemId, quantity + 1) }) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Increase quantity"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationDialog(
    orderId: String,
    tableNumber: Int,
    currentOrderItems: Map<Int, Int>,
    menuItemsMap: Map<String, List<MenuItem>>,
    orderConfirmationState: OrderConfirmationState,
    onConfirmOrder: () -> Unit,
    onDismiss: () -> Unit,
    onResetState: () -> Unit
) {
    val context = LocalContext.current
    var kotSnapshot by remember { mutableStateOf<List<BillPrinterUtil.Companion.KotPrintingItem>>(emptyList()) }
    val allMenuItems =
        remember(menuItemsMap) { menuItemsMap.values.flatten().associateBy { it.id } }
    val totalItems = currentOrderItems.values.sum()
    val totalPrice = currentOrderItems.entries.sumOf {
        val menuItem = allMenuItems[it.key]
        (menuItem?.price ?: 0.0).toDouble() * it.value
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Confirm Order for Table $tableNumber",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Order ID: $orderId",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(currentOrderItems.entries.toList()) { (itemId, quantity) ->
                        val menuItem = allMenuItems[itemId]
                        menuItem?.let {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${it.name} x $quantity")
                                Text("₹%.2f".format(it.price.toDouble() * quantity))
                            }
                        }
                    }
                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Items:", fontWeight = FontWeight.Bold)
                    Text("$totalItems", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Price:", fontWeight = FontWeight.Bold)
                    Text(
                        "₹%.2f".format(totalPrice),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                when (orderConfirmationState) {
                    is OrderConfirmationState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is OrderConfirmationState.Success -> {
                        // Print KOT once when confirmation succeeds using snapshot taken before clearing
                        LaunchedEffect(orderConfirmationState) {
                            val kotText = BillPrinterUtil.generateKOT(
                                kotItems = kotSnapshot,
                                headerLeft = "Token: $orderId",
                                tableLabel = "Table $tableNumber"
                            )
                            BillPrinterUtil.printWithChooser(
                                context,
                                kotText
                            ) { ok, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                        Text(
                            "Order Confirmed!",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Button(onClick = onDismiss) {
                            Text("Done")
                        }
                    }

                    is OrderConfirmationState.Error -> {
                        Text(
                            "Error: ${orderConfirmationState.message}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Button(onClick = onConfirmOrder) {
                                Text("Try Again")
                            }
                            Button(onClick = onDismiss) {
                                Text("Cancel")
                            }
                        }
                    }

                    else -> {

                        SlideToConfirm(
                            text = "Slide to Confirm Order",
                            onConfirmation = {
                                // Snapshot current items before ViewModel clears them
                                val allMenuItems = menuItemsMap.values.flatten().associateBy { it.id }
                                kotSnapshot = currentOrderItems.entries
                                    .filter { it.value > 0 }
                                    .mapNotNull { (itemId, qty) ->
                                        val mi = allMenuItems[itemId]
                                        mi?.let {
                                            BillPrinterUtil.Companion.KotPrintingItem(
                                                menu_name = it.name,
                                                quantity = qty
                                            )
                                        }
                                    }
                                onConfirmOrder()
                            },
                            trackColor = Red80,
                            thumbColor = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                        )

                        Button(onClick = onDismiss) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}