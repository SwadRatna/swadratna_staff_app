package com.swadratna.swadratna_staff.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.swadratna.swadratna_staff.data.remote.model.Category
import com.swadratna.swadratna_staff.data.remote.model.MenuItem
import com.swadratna.swadratna_staff.ui.components.CategoryItem
import com.swadratna.swadratna_staff.ui.components.SearchBar
import com.swadratna.swadratna_staff.ui.components.SwipeRefreshContainer
import com.swadratna.swadratna_staff.ui.components.NetworkTopSnackbarHost
import com.swadratna.swadratna_staff.utils.CurrencyUtils


import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    viewModel: InventoryViewModel = hiltViewModel()
) {

    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredMenuItems by viewModel.filteredMenuItems.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val locationId by viewModel.staffLocationId.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    LaunchedEffect(locationId) {
        locationId?.let {
            viewModel.getMenuItems(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            SwipeRefreshContainer(
                isRefreshing = isLoading,
                onRefresh = {
                    locationId?.let {
                        viewModel.getMenuItems(it)
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {

                    Column(modifier = Modifier.padding(8.dp)) {
                        SearchBar(
                            hintText = "Search Menu Items...",
                            searchQuery, modifier = Modifier
                                .fillMaxWidth(),
                            onQueryChanged = { newQuery ->
                                viewModel.onSearchQueryChanged(newQuery)
                            }
                        )

                        Row(modifier = Modifier.padding(8.dp)) {
                            CategoryList(
                                categories = categories,
                                selectedCategory = selectedCategory,
                                onCategorySelected = { category ->
                                    viewModel.onCategorySelected(category)
                                },
                                modifier = Modifier.weight(0.3f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Column(modifier = Modifier.weight(0.7f)) {
                                selectedCategory?.let { category ->
                                    CategoryHeader(
                                        category = category,
                                        onAvailabilityChanged = { isAvailable ->
                                            viewModel.onCategoryAvailabilityChanged(category, isAvailable)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                
                                MenuItemList(
                                    menuItems = filteredMenuItems, onAvailabilityChanged = { item, isAvailable ->
                                        viewModel.onAvailabilityChanged(item, isAvailable)
                                    }
                                )
                            }
                        }
                    }

                    NetworkTopSnackbarHost(
                        isOnline = isOnline,
                        onRefresh = { locationId?.let { viewModel.getMenuItems(it) } },
                        modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(
    category: Category,
    onAvailabilityChanged: (Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Action") },
            text = { 
                Text(
                    if (category.isAvailable) 
                        "This category will no longer be visible to users. Do you want to proceed?" 
                    else 
                        "This category will be visible to users. Do you want to proceed?"
                ) 
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onAvailabilityChanged(!category.isAvailable)
                }) {
                    Text("Proceed")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = category.isAvailable,
                onCheckedChange = { showDialog = true }
            )
        }
        Divider()
    }
}

@Composable
fun CategoryList(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryItem(
                category = category,
                isSelected = category.id == selectedCategory?.id,
                onCategorySelected = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun MenuItemList(
    menuItems: List<MenuItem>,
    onAvailabilityChanged: (MenuItem, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(menuItems) { item ->
            MenuItemCard(
                item = item, onAvailabilityChanged = { isAvailable ->
                    onAvailabilityChanged(item, isAvailable)
                })
        }
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    onAvailabilityChanged: (Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Action") },
            text = { Text("This menu item will no longer be visible to users. Do you want to proceed?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onAvailabilityChanged(!item.isAvailable)
                }) {
                    Text("Proceed")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (item.isAvailable) 1f else 0.6f)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Image
            AsyncImage(
                model = item.image,
                contentDescription = item.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Veg/Non-Veg Icon
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(1.dp, if(item.isVegetarian) Color.Green else Color.Red)
                            .padding(2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(if(item.isVegetarian) Color.Green else Color.Red, CircleShape))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Price
                val discount = item.discountedPrice ?: 0.0
                val displayPrice = if (discount > 0 && discount < item.price) discount else item.price
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = CurrencyUtils.formatPrice(displayPrice),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (discount > 0 && discount < item.price) {
                         Spacer(modifier = Modifier.width(8.dp))
                         Text(
                             text = CurrencyUtils.formatPrice(item.price),
                             style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                             color = MaterialTheme.colorScheme.onSurfaceVariant
                         )
                    }
                }
            }

            Switch(
                checked = item.isAvailable,
                onCheckedChange = { showDialog = true }
            )
        }
    }
}
