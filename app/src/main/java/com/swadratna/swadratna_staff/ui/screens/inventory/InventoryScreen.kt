package com.swadratna.swadratna_staff.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

data class MenuItem(
    val id: Int,
    val name: String,
    val price: String,
    val category: String,
    var isAvailable: Boolean
)

data class Category(
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen() {
    val categories = listOf(
        Category("Chinese"), Category("Breakfast"), Category("Snacks"),
        Category("Biryani"), Category("South Indian"), Category("Veg Gravy"),
        Category("Non-Veg"), Category("Bread"), Category("Rice")
    )
    
    val menuItems = remember {
        mutableStateListOf(
            MenuItem(1, "Kadhai Chicken", "IDR 1500", "Non-Veg", true),
            MenuItem(2, "Butter Chicken", "IDR 1600", "Non-Veg", true),
            MenuItem(3, "Chicken Curry", "IDR 1400", "Non-Veg", false),
            MenuItem(4, "Egg Curry", "IDR 1000", "Non-Veg", true),
            MenuItem(5, "Egg Salade", "IDR 800", "Snacks", true),
            MenuItem(6, "Chicken Soup", "IDR 900", "Snacks", false),
            MenuItem(7, "Grilled Chicken", "IDR 1700", "Non-Veg", true),
            MenuItem(8, "Egg Noodles", "IDR 1200", "Chinese", true)
        )
    }

    var selectedCategory by remember { mutableStateOf(categories.first { it.name == "Non-Veg" }) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredMenuItems = menuItems.filter {
        it.category == selectedCategory.name && it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Management", fontWeight = FontWeight.Bold) },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("A", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
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
                    onAvailabilityChanged = { item, isAvailable ->
                       val index = menuItems.indexOfFirst { it.id == item.id }
                        if (index != -1) {
                            menuItems[index] = menuItems[index].copy(isAvailable = isAvailable)
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
            CategoryItem(
                category = category,
                isSelected = category.name == selectedCategory.name,
                onCategorySelected = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onCategorySelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onCategorySelected),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.name,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MenuItemList(
    menuItems: List<MenuItem>,
    onAvailabilityChanged: (MenuItem, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(menuItems) { item ->
            MenuItemCard(
                item = item,
                onAvailabilityChanged = { isAvailable ->
                    onAvailabilityChanged(item, isAvailable)
                }
            )
        }
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    onAvailabilityChanged: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.price, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            Switch(
                checked = item.isAvailable,
                onCheckedChange = onAvailabilityChanged
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 823)
@Composable
fun InventoryScreenPreview() {
    InventoryScreen()
}