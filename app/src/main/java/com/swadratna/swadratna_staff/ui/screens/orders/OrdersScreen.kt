package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.room.util.TableInfo
import com.swadratna.swadratna_staff.data.remote.model.KotX
import com.swadratna.swadratna_staff.data.remote.model.OrderDetailsX
import com.swadratna.swadratna_staff.data.remote.model.OrderXX


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    navController: NavController,
    orderID: String?,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val orderDetailsXState by viewModel.detailedOrderState.collectAsStateWithLifecycle()

    LaunchedEffect(orderID) {
        orderID?.let {
            viewModel.getOrderDetail(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        },
        bottomBar = {
            if (orderDetailsXState is OrderDetailsXState.Success) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { /* TODO: Implement Generate Bill logic */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("GENERATE BILL", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { paddingValues ->
        when (orderDetailsXState) {
            is OrderDetailsXState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is OrderDetailsXState.Success -> {
                val orderDetails = (orderDetailsXState as OrderDetailsXState.Success).order
                SuccessLayout(orderDetails, paddingValues)
            }
            is OrderDetailsXState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error: ${(orderDetailsXState as OrderDetailsXState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            OrderDetailsXState.Idle -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(text = "Loading order details...")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------

@Composable
fun SuccessLayout(orderDetails: OrderDetailsX, paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. ORDER SUMMARY CARD
        item {
            OrderSummaryCard(orderDetails.order)
        }

        // 2. KOT LIST HEADING
        item {
            Text(
                text = "Kitchen Orders (KOTs)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // 3. KOT ITEMS
        items(orderDetails.kots) { kot ->
            KotCard(kot)
        }
    }
}

// ---------------------------------------------------------------------------------------------

@Composable
fun OrderSummaryCard(order: OrderXX) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Order ID
            Text(
                text = "Order ID: ${order.id}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(getStatusColor(order.order_status).copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Status",
                    tint = getStatusColor(order.order_status),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = order.order_status.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = getStatusColor(order.order_status)
                )
            }
            // Add other relevant summary details here (e.g., table number, time)
        }
    }
}

@Composable
fun KotCard(kot: KotX) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // KOT Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "KOT #${kot.kot_number}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = kot.status.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = getStatusColor(kot.status)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // KOT Items List
            kot.items.forEach { kotItem ->

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${kotItem.menu_item.name}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${kotItem.menu_item.description}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Qty: ${kotItem.quantity}",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------

@Composable
fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "pending" -> Color(0xFFFF9800) // Amber
        "prepared" -> Color(0xFF4CAF50) // Green
        "served" -> Color(0xFF2196F3) // Blue
        "cancelled" -> MaterialTheme.colorScheme.error // Red
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}