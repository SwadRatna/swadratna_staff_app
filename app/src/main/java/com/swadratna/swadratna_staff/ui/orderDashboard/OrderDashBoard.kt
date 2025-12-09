package com.swadratna.swadratna_staff.ui.orderDashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.R
import com.swadratna.swadratna_staff.data.remote.model.OrderListItem
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.components.SearchBar
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.swadratna.swadratna_staff.ui.components.NetworkTopSnackbarHost
import com.swadratna.swadratna_staff.ui.components.SwipeRefreshContainer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDashboard(
    modifier: Modifier = Modifier,
    viewModel: OrdersDashboardViewModel = hiltViewModel(),
    navController: NavController
) {
    val orders by viewModel.orders.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            viewModel.refreshOrders()
        }
    }

    LaunchedEffect(isOnline) {
        if (isOnline) {
            viewModel.refreshOrders()
        }
    }

    SwipeRefreshContainer(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshOrders() }
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Search Bar
            SearchBar(
                hintText = "Search Menu Items...",
                query = searchQuery,
                modifier = Modifier
                    .fillMaxWidth(),
                onQueryChanged = { newQuery ->
                    viewModel.updateSearchQuery(newQuery)
                },

            )

            // Filter Chips
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { viewModel.updateFilter(it) }
            )

            // Content based on state
            when {
                loading && orders.isEmpty() -> {
                    // Show loading indicator when initially loading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    }
                }
                error != null && orders.isEmpty() && isOnline -> {
                    // Show error state
                    ErrorState(
                        error = error,
                        onRetry = { viewModel.fetchOrders(page = 1, limit = 50) }
                    )
                }
                orders.isEmpty() -> {
                    // Show empty state
                    EmptyState(
                        onRefresh = { viewModel.fetchOrders(page = 1, limit = 50) }
                    )
                }
                else -> {
                    // Show orders list
                    LazyColumn(
                        contentPadding = PaddingValues(0.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(orders) { order ->
                            OrderCard(order = order, navController)
                        }
                    }
                }
            }
        }

        // Show loading indicator as overlay when refreshing with existing data
        if (loading && orders.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp))
            }
        }

        NetworkTopSnackbarHost(
            isOnline = isOnline,
            onRefresh = { viewModel.refreshOrders() },
            modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
        )
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedFilter: String?,
    onFilterSelected: (String) -> Unit
) {
    val filterOptions = listOf("All", "Active", "Completed")
    
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filterOptions.forEach { filter ->
            val isSelected = selectedFilter == filter
            ElevatedFilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                colors = FilterChipDefaults.elevatedFilterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                trailingIcon = if (isSelected && filter != "All") {
                    {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear $filter filter",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Only show clear button if a filter is selected and it's not "All"
        if (selectedFilter != null && selectedFilter != "All") {
            Button(
                onClick = { onFilterSelected("All") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Clear")
            }
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun EmptyState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No Orders Found",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "There are no orders matching your criteria",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRefresh,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh")
        }
    }
}

@Composable
fun ErrorState(error: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error ?: "Failed to load orders. Please try again.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Retry")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
fun OrderCard(order: OrderListItem, navController: NavController) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()

            .clickable(enabled = true, onClick = {
                navController.navigate(NavigationRoute.OrderTaking.createRoute(
                    tableNumber = order.table.id,
                    orderId = order.id,
                    showMenuTab = false,
                    showOrdersTab = false,
                    defaultTab = 1
                ))
            }),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Order ID and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (order.orderStatus) {
                        "New" -> MaterialTheme.colorScheme.errorContainer
                        "In Progress" -> MaterialTheme.colorScheme.tertiaryContainer
                        "Completed" -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    Text(
                        text = order.orderStatus,
                        color = when (order.orderStatus) {
                            "New" -> MaterialTheme.colorScheme.onErrorContainer
                            "In Progress" -> MaterialTheme.colorScheme.onTertiaryContainer
                            "Completed" -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer and Table Info in one row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Customer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    order?.user?.name?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Table Info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_table),
                        contentDescription = "Table",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Table ${order.table.table_id}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price and Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Pricing
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${order.totalValue}",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (order.discount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(-₹${order.discount})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Right side: Time
                Text(
                    text = formatOrderTime(order.orderDate),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatOrderTime(orderDate: String): String {
    return try {
        orderDate
    } catch (e: Exception) {
        orderDate
    }
}
