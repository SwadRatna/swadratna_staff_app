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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swadratna.swadratna_staff.ui.screens.inventory.MenuItem

data class Category(
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTakingScreen(
    tableNumber: Int,
    customerName: String,
    onBack: () -> Unit
) {
    val categories = listOf(
        Category("Chinese"), Category("Breakfast"), Category("Snacks"),
        Category("Biryani"), Category("South Indian"), Category("Veg Gravy"),
        Category("Non-Veg"), Category("Bread"), Category("Rice")
    )

    val allMenuItems = remember {
        mutableStateListOf(
            MenuItem(1, "Kadhai Chicken", "IDR 1500", "Non-Veg", true),
            MenuItem(2, "Butter Chicken", "IDR 1600", "Non-Veg", true),
            MenuItem(3, "Chicken Curry", "IDR 1400", "Non-Veg", true),
            MenuItem(4, "Egg Curry", "IDR 1000", "Non-Veg", true),
            MenuItem(5, "Egg Salade", "IDR 800", "Snacks", true),
            MenuItem(6, "Chicken Soup", "IDR 900", "Snacks", false), // Example of unavailable
            MenuItem(7, "Grilled Chicken", "IDR 1700", "Non-Veg", true),
            MenuItem(8, "Egg Noodles", "IDR 1200", "Chinese", true)
        )
    }

    var selectedCategory by remember { mutableStateOf(categories.first { it.name == "Non-Veg" }) }
    var searchQuery by remember { mutableStateOf("") }
    val orderItems = remember { mutableStateMapOf<Int, Int>() }

    val filteredMenuItems = allMenuItems.filter {
        it.isAvailable && it.category == selectedCategory.name && it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders: Table $tableNumber ($customerName)", fontSize = 16.sp) },
                actions = {
                    Button(onClick = { /* TODO: View Bill */ }) {
                        Text("View Bill")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        },
        floatingActionButton = {
            Button(
                onClick = { /* TODO: Generate KOT */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Generate KOT", modifier = Modifier.padding(8.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(bottom = 60.dp)) {
            SearchBar(searchQuery) { newQuery ->
                searchQuery = newQuery
            }
            Row(modifier = Modifier.padding(16.dp)) {
                CategoryList(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = category
                    },
                    modifier = Modifier.weight(0.3f)
                )
                Spacer(modifier = Modifier.width(16.dp))
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
                    modifier = Modifier.weight(0.7f)
                )
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text("Search Menu") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
        )
    )
}

@Composable
fun CategoryList(
    categories: List<Category>,
    selectedCategory: Category,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (category.name == selectedCategory.name) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .clickable { onCategorySelected(category) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.name,
                    color = if (category.name == selectedCategory.name) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
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
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.price, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }

            if (quantity == 0) {
                Button(onClick = { onUpdateOrder(1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Text("Add")
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { onUpdateOrder(quantity - 1) }, Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Remove")
                    }
                    Text(quantity.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onUpdateOrder(quantity + 1) }, Modifier.size(24.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 823)
@Composable
fun OrderTakingScreenPreview() {
    OrderTakingScreen(tableNumber = 4, customerName = "Vivek Kumar", onBack = {})
}