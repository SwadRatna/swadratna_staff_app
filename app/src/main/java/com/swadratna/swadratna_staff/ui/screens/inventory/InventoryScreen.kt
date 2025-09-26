package com.swadratna.swadratna_staff.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel = hiltViewModel()) {

    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredMenuItems by viewModel.filteredMenuItems.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        SearchBar(searchQuery) { newQuery ->
            viewModel.onSearchQueryChanged(newQuery)
        }
        Row(modifier = Modifier.padding(16.dp)) {
            CategoryList(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    viewModel.onCategorySelected(category)
                },
                modifier = Modifier.weight(0.3f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            MenuItemList(
                menuItems = filteredMenuItems, onAvailabilityChanged = { item, isAvailable ->
                    viewModel.onAvailabilityChanged(item, isAvailable)
                }, modifier = Modifier.weight(0.7f)
            )
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
                isSelected = category.categoryId == selectedCategory?.categoryId,
                onCategorySelected = { onCategorySelected(category) })
        }
    }
}

@Composable
fun CategoryItem(
    category: Category, isSelected: Boolean, onCategorySelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onCategorySelected), contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.categoryName,
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
    item: MenuItem, onAvailabilityChanged: (Boolean) -> Unit
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
                Text(
                    "IDR ${item.price}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            Switch(
                checked = item.isAvailable, onCheckedChange = onAvailabilityChanged
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 823)
@Composable
fun InventoryScreenPreview() {
    InventoryScreen()
}