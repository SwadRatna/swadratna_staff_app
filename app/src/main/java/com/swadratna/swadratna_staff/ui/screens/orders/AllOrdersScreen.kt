package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.data.remote.model.KotX
import com.swadratna.swadratna_staff.data.remote.model.OrderDetailsX
import com.swadratna.swadratna_staff.data.remote.model.OrderXX
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.screens.kot.KotStatusUpdateState
import com.swadratna.swadratna_staff.ui.screens.kot.KotViewModel
import com.swadratna.swadratna_staff.utils.BillPrinterUtil
import com.swadratna.swadratna_staff.utils.rememberBluetoothPermissionLauncher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.swadratna.swadratna_staff.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    orderID: String?,
    viewModel: OrderManagementViewModel,
    kotViewModel: KotViewModel = hiltViewModel()
) {
    val orderDetailsXState by viewModel.detailedOrderState.collectAsStateWithLifecycle()
    val kotStatusUpdateState by kotViewModel.kotStatusUpdateState.collectAsStateWithLifecycle()

    LaunchedEffect(orderID) {
        orderID?.let {
            viewModel.getOrderDetail(it)
        }
    }

    LaunchedEffect(kotStatusUpdateState) {
        if (kotStatusUpdateState is KotStatusUpdateState.Success) {
            orderID?.let { viewModel.getOrderDetail(it) }
            kotViewModel.resetStatusUpdateState()
        }
    }

    val userHaveOrders = remember(orderDetailsXState) {
        if (orderDetailsXState is OrderDetailsXState.Success) {
            val orderDetails = (orderDetailsXState as OrderDetailsXState.Success).order
            orderDetails.kots?.isNotEmpty() == true
        } else {
            false
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (userHaveOrders) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            navController.navigate(
                                NavigationRoute.Bill.createRoute(
                                    orderID ?: ""
                                )
                            )
                        },
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is OrderDetailsXState.Success -> {
                val orderDetails = (orderDetailsXState as OrderDetailsXState.Success).order
                SuccessLayout(
                    userHaveOrders,
                    orderDetails,
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onUpdateKotStatus = { kotId, status ->
                        kotViewModel.updateKotStatus(kotId, status)
                    }
                )
            }

            is OrderDetailsXState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(paddingValues)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${(orderDetailsXState as OrderDetailsXState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            OrderDetailsXState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Loading order details...")
                }
            }
        }
    }
}


@Composable
fun SuccessLayout(
    userHaveOrders: Boolean,
    orderDetails: OrderDetailsX,
    modifier: Modifier,
    onUpdateKotStatus: (Int, String) -> Unit
) {
    if (userHaveOrders) {
        LazyColumn(
            modifier = modifier,
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

            items(orderDetails.kots.orEmpty()) { kot ->
                KotCard(
                    kot = kot,
                    tableLabel = orderDetails.table?.table_id,
                    customerName = orderDetails.user?.name,
                    onUpdateStatus = onUpdateKotStatus
                )
            }

        }

    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) ,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "You haven't ordered yet. Please order to see your Orders.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------

@Composable
fun OrderSummaryCard(order: OrderXX) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
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
fun KotCard(
    kot: KotX,
    tableLabel: String?,
    customerName: String?,
    onUpdateStatus: (Int, String) -> Unit
) {
    val context = LocalContext.current
    var showStatusDialog by remember { mutableStateOf(false) }

    val performPrint = rememberBluetoothPermissionLauncher {
        val kotItems = kot.items?.map {
            BillPrinterUtil.Companion.KotPrintingItem(
                menu_name = it.menu_item.name,
                quantity = it.quantity
            )
        } ?: emptyList()

        var createdAt = Date()
        try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            createdAt = isoFormat.parse(kot.created_at) ?: Date()
        } catch (e: Exception) {
            try {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                createdAt = isoFormat.parse(kot.created_at) ?: Date()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }

        val kotText = BillPrinterUtil.generateKOT(
            kotItems = kotItems,
            headerLeft = "KOT #${kot.kot_number}",
            tableLabel = tableLabel,
            customerName = customerName,
            createdAt = createdAt
        )

        BillPrinterUtil.printWithChooser(context, kotText) { success, msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    if (showStatusDialog) {
        StatusUpdateDialog(
            currentStatus = kot.status,
            onDismiss = { showStatusDialog = false },
            onStatusSelected = { newStatus ->
                onUpdateStatus(kot.id, newStatus)
                showStatusDialog = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // KOT Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "KOT #${kot.kot_number}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showStatusDialog = true }
                    ) {
                        Text(
                            text = kot.status.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = getStatusColor(kot.status)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Status",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(onClick = {
                    performPrint()
                }) {
                    Icon(painter = painterResource(R.drawable.ic_recipt), contentDescription = "Print KOT")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // KOT Items List (null-safe)
            kot.items.orEmpty().forEach { kotItem ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

@Composable
fun StatusUpdateDialog(
    currentStatus: String,
    onDismiss: () -> Unit,
    onStatusSelected: (String) -> Unit
) {
    val statuses = listOf("pending", "prepared", "served", "cancelled")
    var selectedStatus by remember { mutableStateOf(currentStatus.lowercase()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Update KOT Status") },
        text = {
            Column {
                statuses.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStatus = status }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (status == selectedStatus),
                            onClick = { selectedStatus = status }
                        )
                        Text(
                            text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onStatusSelected(selectedStatus) }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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