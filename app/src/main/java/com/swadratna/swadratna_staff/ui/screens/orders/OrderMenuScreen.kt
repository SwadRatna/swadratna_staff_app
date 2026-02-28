package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
import androidx.compose.ui.text.style.TextDecoration
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
import androidx.compose.ui.res.painterResource
import com.swadratna.swadratna_staff.R
import com.swadratna.swadratna_staff.data.remote.model.Category
import com.swadratna.swadratna_staff.data.remote.model.MenuItem
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.components.CategoryItem
import com.swadratna.swadratna_staff.ui.components.SearchBar
import com.swadratna.swadratna_staff.ui.components.SlideToConfirm
import com.swadratna.swadratna_staff.ui.theme.Red80
import com.swadratna.swadratna_staff.utils.BillPrinterUtil
import com.swadratna.swadratna_staff.utils.CurrencyUtils
import com.swadratna.swadratna_staff.ui.components.NetworkTopSnackbarHost
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
    val isOnline by viewModel.isOnline.collectAsState()
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

    LaunchedEffect(isOnline) {
        if (isOnline) {
            staffLocationId?.let { viewModel.getMenuItems(it, "") }
        }
    }

    LaunchedEffect(categories) {
        if (categories.isNotEmpty()) {
            if (selectedCategoryId == null || categories.none { it.id == selectedCategoryId }) {
                selectedCategoryId = categories.first().id
            }
        }
    }

    val currentMenuItems: List<MenuItem> = remember(selectedCategoryId, menuItemsMap) {
        selectedCategoryId?.let { id ->
            menuItemsMap[id.toString()]
        } ?: emptyList()
    }

    val filteredMenuItems = remember(currentMenuItems, searchQuery, menuItemsMap) {
        if (searchQuery.isNotBlank()) {
            menuItemsMap.values.flatten().filter { item ->
                item.isAvailable && item.name.contains(searchQuery, ignoreCase = true)
            }
        } else {
            currentMenuItems.filter { item ->
                item.isAvailable
            }
        }
    }

    // Create a map of all menu items for efficient lookup and total calculation
    val allMenuItems = remember(menuItemsMap) {
        menuItemsMap.values.flatten().associateBy { it.id }
    }

    val totalItemsInOrder = currentOrderItems.values.sum()

    // Calculate total price using discounted prices
    val orderTotalPrice = remember(currentOrderItems, allMenuItems) {
        currentOrderItems.entries.sumOf { (itemId, quantity) ->
            val menuItem = allMenuItems[itemId]
            val discount = menuItem?.discountedPrice ?: 0.0
            val price = if (discount > 0 && discount < (menuItem?.price ?: 0.0)) discount else menuItem?.price ?: 0.0
            price * quantity
        }
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        )
        {
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

                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val screenWidth = maxWidth
                    val categoryWidth = screenWidth * 0.30f
                    
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AnimatedVisibility(
                            visible = searchQuery.isEmpty(),
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut()
                        ) {
                            Row {
                                CategoryList(
                                    categories = categories,
                                    selectedCategoryId = selectedCategoryId, // Pass ID
                                    onCategorySelected = { selectedCategoryId = it?.id }, // Use category.id
                                    modifier = Modifier.width(categoryWidth)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }

                        MenuItemList(
                            menuItems = filteredMenuItems,
                            orderItems = currentOrderItems,
                            onUpdateOrder = { itemId, quantity ->
                                viewModel.updateOrderItem(itemId, quantity)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (showOrderConfirmationDialog) {
            OrderConfirmationDialog(
                orderId = orderId,
                tableStringId = tableStringId,
                currentOrderItems = currentOrderItems,
                allMenuItems = allMenuItems,
                totalPrice = orderTotalPrice,
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
                allMenuItems = allMenuItems,
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
                totalPrice = orderTotalPrice,
                onPillClick = { showExpandedOrderSummary = true },
                onOrderClick = { showOrderConfirmationDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
        NetworkTopSnackbarHost(
            isOnline = isOnline,
            onRefresh = { staffLocationId?.let {viewModel.getMenuItems(it, "") } },
            modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
        )
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
        modifier = modifier.padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(categories) { category ->
            // Check selection using ID
            CompactCategoryItem(
                category = category,
                isSelected = category.id == selectedCategoryId,
                onCategorySelected = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CompactCategoryItem(
    category: Category,
    isSelected: Boolean,
    onCategorySelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp) // Thinner height
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onCategorySelected)
            .padding(horizontal = 4.dp), // Less padding
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.name,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium, // Smaller text
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MenuItemList(
    menuItems: List<MenuItem>,
    orderItems: Map<Int, Int>,
    onUpdateOrder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Add padding for bottom summary pill
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
            // Height is determined by content, but we can set a min height or fixed height if needed.
            // Let it wrap content for now, or set a fixed height for uniformity.
            .height(220.dp), 
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f) // Take up about 55% of the card height
            ) {
                if (item.image.isNotBlank()) {
                    AsyncImage(
                        model = item.image,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder if no image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Menu, // Or a food icon
                            contentDescription = "No Image",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .padding(4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val discount = item.discountedPrice ?: 0.0
                    if (discount > 0 && discount < item.price) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = CurrencyUtils.formatPrice(item.price),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = CurrencyUtils.formatPrice(discount),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = CurrencyUtils.formatPrice(item.price),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Quantity Controls
                if (quantity == 0) {
                    Button(
                        onClick = { onUpdateOrder(1) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    ) {
                        Text("ADD", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    RoundedCornerShape(4.dp)
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
                                Icons.Filled.Delete, // Or Minus if > 1, but logic handles 0
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = quantity.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(4.dp)
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
                                modifier = Modifier.size(16.dp)
                            )
                        }
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
    allMenuItems: Map<Int, MenuItem>,
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
                    val menuItem = allMenuItems[itemId]
                    if (menuItem != null) {
                        val discount = menuItem.discountedPrice ?: 0.0
                        val price = if (discount > 0 && discount < menuItem.price) discount else menuItem.price
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
                                    val discount = menuItem.discountedPrice ?: 0.0
                                    if (discount > 0 && discount < menuItem.price) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = CurrencyUtils.formatPrice(menuItem.price),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                textDecoration = TextDecoration.LineThrough
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = CurrencyUtils.formatPrice(discount),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = CurrencyUtils.formatPrice(price),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
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
                                            painter = painterResource(R.drawable.ic_remove),
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
                    text = CurrencyUtils.formatPrice(totalPrice),
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
        val discount = menuItem?.discountedPrice ?: 0.0
        val price = if (discount > 0 && discount < (menuItem?.price ?: 0.0)) discount else menuItem?.price ?: 0.0
        price * it.value
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
                    text = CurrencyUtils.formatPrice(totalPrice.toDouble()),
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
                                val discount = it.discountedPrice ?: 0.0
                                if (discount > 0 && discount < it.price) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = CurrencyUtils.formatPrice(it.price),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = CurrencyUtils.formatPrice(discount),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    Text(
                                        CurrencyUtils.formatPrice(it.price),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
    tableStringId: String,
    currentOrderItems: Map<Int, Int>,
    allMenuItems: Map<Int, MenuItem>,
    totalPrice: Double,
    orderConfirmationState: OrderConfirmationState,
    onConfirmOrder: () -> Unit,
    onDismiss: () -> Unit,
    onResetState: () -> Unit
) {
    val context = LocalContext.current
    var kotSnapshot by remember { mutableStateOf<List<BillPrinterUtil.Companion.KotPrintingItem>>(emptyList()) }
    val totalItems = currentOrderItems.values.sum()

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
                    if (tableStringId == "Parcel") "Confirm Parcel Order" else "Confirm Order for Table $tableStringId",
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
                            val discount = it.discountedPrice ?: 0.0
                            val price = if (discount > 0 && discount < it.price) discount else it.price
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${it.name} x $quantity")
                                Text(CurrencyUtils.formatPrice(price * quantity))
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
                        CurrencyUtils.formatPrice(totalPrice),
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
                                tableLabel = if (tableStringId == "Parcel") "Parcel" else "Table $tableStringId"
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