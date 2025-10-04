package com.swadratna.swadratna_staff.ui.screens.orders

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_staff.data.remote.model.MenuItem
import com.swadratna.swadratna_staff.data.remote.model.Category
import com.swadratna.swadratna_staff.data.remote.services.KotItem
import com.swadratna.swadratna_staff.data.remote.services.KotRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTakingScreen(
    tableNumber: Int,
    orderId: String,
    onBack: () -> Unit,
    viewModel: OrderManagementViewModel = hiltViewModel()
) {
    val menuItems by viewModel.menuItems.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val orderItems = remember { mutableStateMapOf<Int, Int>() }

    LaunchedEffect(Unit) {
        // Assuming a fixed locationId for now, or it needs to be passed as an argument
        viewModel.getMenuItems(1)
    }

    val categories = remember(menuItems) {
        menuItems.map { it.category }.distinct().map {
            Category(it.categoryId , it.categoryName)
        }
    }

    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = categories.first().categoryId
        }
    }

    val filteredMenuItems = menuItems.filter {
        it.isAvailable && it.category.categoryId == selectedCategory && it.name.contains(
            searchQuery,
            ignoreCase = true
        )
    }

    val totalItems = orderItems.values.sum()

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
                        onClick = { /* TODO: Implement View Bill logic */ },
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
        bottomBar = {
            if (totalItems > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = {
                            val kotItems = orderItems.map { (itemId, quantity) ->
                                KotItem(menuItemId = itemId.toString(), quantity = quantity)
                            }
                            viewModel.createKot(KotRequest(tableId = tableNumber.toString(), items = kotItems))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Generate KOT ($totalItems items)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                // Search Bar
                SearchBar(
                    query = searchQuery,
                    onQueryChanged = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Main Content
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Category List
                    CategoryList(
                        categories = categories,
                        selectedCategory = categories.find { it.categoryId == selectedCategory } ?:
                        categories.firstOrNull(),
                        onCategorySelected = { selectedCategory = it?.categoryId },
                        modifier = Modifier.weight(0.35f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Menu Items List
                    MenuItemList(
                        menuItems = filteredMenuItems,
                        orderItems = orderItems,
                        onUpdateOrder = { itemId, quantity ->
                            if (quantity > 0) {
                                orderItems[itemId] = quantity
                            } else {
                                orderItems.remove(itemId)
                            }
                        },
                        modifier = Modifier.weight(0.65f)
                    )
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
    selectedCategory: Category?,
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
                val isSelected = category.categoryName == selectedCategory?.categoryName

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable { onCategorySelected(category) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.categoryName,
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
        // Item Details
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp)
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
                text = "₹${item.price}", // Display price with currency symbol
                color = MaterialTheme.colorScheme.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Quantity Controls
            if (quantity == 0) {
                Button(
                    onClick = { onUpdateOrder(1) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.45f).padding(0.dp).align(Alignment.End)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(18.dp)
                    )
                    Text("ADD", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.45f).align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                CircleShape
                            ).
                            clickable(
                                enabled = true,
                                onClick = {
                                    onUpdateOrder(quantity - 1)
                                }
                            )
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(25.dp)
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
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ).
                            clickable(
                                enabled = true,
                                onClick = {
                                    onUpdateOrder(quantity + 1)
                                }
                            )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
        }
    }
}