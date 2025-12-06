package com.swadratna.swadratna_staff.ui.screens.inventory

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_staff.data.remote.model.Category
import com.swadratna.swadratna_staff.data.remote.model.MenuItem
import com.swadratna.swadratna_staff.ui.components.CategoryItem
import com.swadratna.swadratna_staff.ui.components.SearchBar
import com.swadratna.swadratna_staff.ui.components.SwipeRefreshContainer
import com.swadratna.swadratna_staff.ui.components.NetworkTopSnackbarHost


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel = hiltViewModel()) {

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
                MenuItemList(
                    menuItems = filteredMenuItems, onAvailabilityChanged = { item, isAvailable ->
                        viewModel.onAvailabilityChanged(item, isAvailable)
                    }, modifier = Modifier.weight(0.7f)
                )
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
                onCategorySelected = { onCategorySelected(category) })
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
                Text(item.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Text(
                    "IDR ${item.price}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            Switch(
                checked = item.isAvailable,
                onCheckedChange = { showDialog = true }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 823)
@Composable
fun InventoryScreenPreview() {
    InventoryScreen()
}
