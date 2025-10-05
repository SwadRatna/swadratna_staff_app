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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_staff.data.remote.model.Category // Import your actual Category model
import com.swadratna.swadratna_staff.data.remote.model.MenuItem // Import your actual MenuItem model
import com.swadratna.swadratna_staff.data.remote.services.KotItem
import com.swadratna.swadratna_staff.ui.components.SlideToConfirmButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTakingScreen(
    tableNumber: Int,
    orderId: String,
    onBack: () -> Unit,
    viewModel: OrderManagementViewModel = hiltViewModel()
) {
    // UPDATED STATE FLOWS: Using the new states from the ViewModel
    val categories by viewModel.categories.collectAsState()
    val menuItemsMap by viewModel.menuItemsMap.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentOrderItems by viewModel.currentOrderItems.collectAsState()
    val orderConfirmationState by viewModel.orderConfirmationState.collectAsState()

    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showOrderSummary by remember { mutableStateOf(false) }
    var showOrderConfirmationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getMenuItems("")
    }

    // Initialize or reset selectedCategoryId when categories load
    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id // Use category.id
        }
    }

    // Get menu items for the currently selected category
    val currentMenuItems: List<MenuItem> = remember(selectedCategoryId, menuItemsMap) {
        selectedCategoryId?.let { id ->
            menuItemsMap[id.toString()]
        } ?: emptyList()
    }

    // Filter the items based on the search query
    val filteredMenuItems = remember(currentMenuItems, searchQuery) {
        currentMenuItems.filter { item ->
            item.isAvailable && item.name.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalItemsInOrder = currentOrderItems.values.sum()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Table $tableNumber",
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
                    OutlinedButton(
                        onClick = {  },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Menu,
                            contentDescription = "Bill",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Bill", fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box( modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
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
                    onDismiss = { showOrderConfirmationDialog = false; viewModel.resetOrderConfirmationState() },
                    onResetState = { viewModel.resetOrderConfirmationState() }
                )
            }
             if (totalItemsInOrder > 0) {
                 Card(
                     modifier = Modifier
                         .fillMaxWidth()
                         .align(Alignment.BottomCenter),
                     shape = RoundedCornerShape(12.dp),
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(0.8f))
                 ) {
                     Column(modifier = Modifier
                         .fillMaxWidth()
                         .padding(horizontal = 16.dp, vertical = 8.dp)
                     ) {
                         OrderSummaryCard(
                             currentOrderItems = currentOrderItems,
                             menuItemsMap = menuItemsMap,
                             onUpdateOrder = { itemId, quantity -> viewModel.updateOrderItem(itemId, quantity) },
                             showOrderSummary = showOrderSummary,
                             onToggleSummary = { showOrderSummary = !showOrderSummary }
                         )
                         Button(
                             onClick = { showOrderConfirmationDialog = true },
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .padding(horizontal = 16.dp)
                                 .height(56.dp),
                             shape = RoundedCornerShape(12.dp)
                         ) {
                             Text(
                                 "Order ($totalItemsInOrder items)",
                                 fontSize = 16.sp,
                                 fontWeight = FontWeight.SemiBold
                             )
                         }
                     }

                 }
             }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            Text(
                "Search menu items...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        singleLine = true
    )
}

@Composable
fun CategoryList(
    categories: List<Category>,
    selectedCategoryId: Int?, // Changed type to Int?
    onCategorySelected: (Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        LazyColumn(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(categories) { category ->
                // Check selection using ID
                val isSelected = category.id == selectedCategoryId

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            }
                            else {
                                Color.Transparent
                            }
                        )
                        .clickable { onCategorySelected(category) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.name, // Use category.name
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
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
        modifier = Modifier.fillMaxWidth().height(100.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Item Details
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
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
                                MaterialTheme.colorScheme.primaryContainer,
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
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
fun OrderSummaryCard(
    currentOrderItems: Map<Int, Int>,
    menuItemsMap: Map<String, List<MenuItem>>,
    onUpdateOrder: (Int, Int) -> Unit,
    showOrderSummary: Boolean,
    onToggleSummary: () -> Unit
) {
    val allMenuItems = remember(menuItemsMap) { menuItemsMap.values.flatten().associateBy { it.id } }
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
                    .clickable(onClick = onToggleSummary)
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

            AnimatedVisibility(visible = showOrderSummary) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
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
                                    Text("₹%.2f".format(it.price.toDouble()), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onUpdateOrder(itemId, quantity - 1) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Decrease quantity")
                                    }
                                    Text("$quantity", fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { onUpdateOrder(itemId, quantity + 1) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                                    }
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
    val allMenuItems = remember(menuItemsMap) { menuItemsMap.values.flatten().associateBy { it.id } }
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
                Text("Confirm Order for Table $tableNumber", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Order ID: $orderId", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Divider()

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp),
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
                    Text("₹%.2f".format(totalPrice), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                when (orderConfirmationState) {
                    is OrderConfirmationState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is OrderConfirmationState.Success -> {
                        Text("Order Confirmed!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Button(onClick = onDismiss) {
                            Text("Done")
                        }
                    }
                    is OrderConfirmationState.Error -> {
                        Text("Error: ${orderConfirmationState.message}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Button(onClick = onConfirmOrder) {
                                Text("Try Again")
                            }
                            Button(onClick = onDismiss) {
                                Text("Cancel")
                            }
                        }
                    }
                    else -> {
                        SlideToConfirmButton(
                            onConfirmOrder,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
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