package com.swadratna.swadratna_staff.ui.screens.orders

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.R
import com.swadratna.swadratna_staff.data.remote.model.KotX
import com.swadratna.swadratna_staff.data.remote.model.OrderDetailsX
import com.swadratna.swadratna_staff.data.remote.model.OrderXX
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.components.NetworkTopSnackbarHost
import com.swadratna.swadratna_staff.ui.screens.kot.KotStatusUpdateState
import com.swadratna.swadratna_staff.ui.screens.kot.KotViewModel
import com.swadratna.swadratna_staff.utils.BillPrinterUtil
import com.swadratna.swadratna_staff.utils.rememberBluetoothPermissionLauncher
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

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

    LaunchedEffect(isOnline) {
        if (isOnline) {
            orderID?.let { viewModel.getOrderDetail(it) }
        }
    }

    val orderDetails = (orderDetailsXState as? OrderDetailsXState.Success)?.order

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (orderDetails != null) {
                val isCompleted = orderDetails.order.order_status.equals("completed", ignoreCase = true)
                val haveNoOrderYet = (orderDetails.kots == null || orderDetails.kots.isEmpty() )
                val showBillGenBtn =  if(haveNoOrderYet) false else if(isCompleted) false else true
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
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
                        enabled =  showBillGenBtn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Icon(painterResource(R.drawable.ic_recipt), contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCompleted) "BILL GENERATED" else "GENERATE BILL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
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
                    NetworkTopSnackbarHost(
                        isOnline = isOnline,
                        onRefresh = { orderID?.let { viewModel.getOrderDetail(it) } },
                        modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
                    )
                }
            }

            is OrderDetailsXState.Success -> {
                val order = (orderDetailsXState as OrderDetailsXState.Success).order
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    SuccessLayout(
                        orderDetails = order,
                        modifier = Modifier.fillMaxSize(),
                        onUpdateKotStatus = { kotId, status ->
                            kotViewModel.updateKotStatus(kotId, status)
                        }
                    )
                    NetworkTopSnackbarHost(
                        isOnline = isOnline,
                        onRefresh = { orderID?.let { viewModel.getOrderDetail(it) } },
                        modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
                    )
                }
            }

            is OrderDetailsXState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(paddingValues)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    if (isOnline) {
                        Text(
                            text = "Error: ${(orderDetailsXState as OrderDetailsXState.Error).message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    NetworkTopSnackbarHost(
                        isOnline = isOnline,
                        onRefresh = { orderID?.let { viewModel.getOrderDetail(it) } },
                        modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
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
                    NetworkTopSnackbarHost(
                        isOnline = isOnline,
                        onRefresh = { orderID?.let { viewModel.getOrderDetail(it) } },
                        modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
                    )
                    Text(text = "Loading order details...")
                }
            }
        }
    }
}


@Composable
fun SuccessLayout(
    orderDetails: OrderDetailsX,
    modifier: Modifier,
    onUpdateKotStatus: (Int, String) -> Unit
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // 1. Customer & Table Info Card
        item {
            CustomerTableCard(orderDetails)
        }

        // 2. Order Summary (ID, Time, Status)
//        item {
//            OrderSummaryCard(orderDetails.order)
//        }

        // 3. KOT List
        item {
            Text(
                text = "Kitchen Orders (KOTs)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (orderDetails.kots.isNullOrEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No KOTs placed yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(orderDetails.kots) { kot ->
                KotCard(
                    kot = kot,
                    tableLabel = orderDetails.table.table_id,
                    customerName = orderDetails.user.name,
                    onUpdateStatus = onUpdateKotStatus
                )
            }
        }
    }
}

@Composable
fun CustomerTableCard(orderDetails: OrderDetailsX) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TABLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = orderDetails.table.table_id,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Check if the user object exists first.
                orderDetails.user?.let { user ->
                    // The entire block below will only execute if orderDetails.user is NOT null.
                    Column(horizontalAlignment = Alignment.End) {

                        // 1. User Name Row - Check if the name exists before rendering the row
                        user.name?.let { name ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // 2. User Email Row - Check if the email exists and is not empty
                        if (user.email?.isNotEmpty() == true) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val address = orderDetails.location.address
                val fullAddress = listOfNotNull(
                    address.street_1, 
                    address.locality, 
                    address.city
                ).filter { it.isNotEmpty() }.joinToString(", ")
                
                Text(
                    text = fullAddress.ifEmpty { "Location not available" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}

//@Composable
//fun OrderSummaryCard(order: OrderXX) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
//        elevation = CardDefaults.cardElevation(2.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            // Header: ID and Status
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column {
//                    Text(
//                        text = "Order #${order.id}",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            Icons.Default.DateRange,
//                            contentDescription = null,
//                            modifier = Modifier.size(14.dp),
//                            tint = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = formatDate(order.order_date),
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//
//                Surface(
//                    shape = RoundedCornerShape(50),
//                    color = getStatusColor(order.order_status).copy(alpha = 0.1f)
//                ) {
//                    Row(
//                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .size(8.dp)
//                                .background(getStatusColor(order.order_status), CircleShape)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = order.order_status.uppercase(),
//                            style = MaterialTheme.typography.labelMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = getStatusColor(order.order_status)
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Financials
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Column {
//                    Text(
//                        text = "Subtotal",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        text = "₹${order.order_value}",
//                        style = MaterialTheme.typography.bodyLarge,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                }
//                if (order.discount > 0) {
//                    Column(horizontalAlignment = Alignment.End) {
//                        Text(
//                            text = "Discount",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.error
//                        )
//                        Text(
//                            text = "-₹${order.discount}",
//                            style = MaterialTheme.typography.bodyLarge,
//                            fontWeight = FontWeight.SemiBold,
//                            color = MaterialTheme.colorScheme.error
//                        )
//                    }
//                }
//                Column(horizontalAlignment = Alignment.End) {
//                    Text(
//                        text = "Total",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        text = "₹${order.total_value}",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        }
//    }
//}

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
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "KOT #${kot.kot_number}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = getStatusColor(kot.status).copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, getStatusColor(kot.status).copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { showStatusDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = kot.status.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = getStatusColor(kot.status)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(10.dp),
                                    tint = getStatusColor(kot.status)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDate(kot.created_at),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(
                    onClick = { performPrint() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_recipt), 
                        contentDescription = "Print",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            // Items
            Column(modifier = Modifier.padding(16.dp)) {
                kot.items.orEmpty().forEachIndexed { index, kotItem ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Veg/Non-Veg Indicator
                        VegNonVegIcon(
                            isVegetarian = kotItem.menu_item?.isVegetarian == true,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Details
                        Column(modifier = Modifier.weight(1f)) {
                            val itemName = kotItem.menu_item?.name ?: "Unknown Item"
                            Text(
                                text = itemName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!kotItem.menu_item?.description.isNullOrEmpty()) {
                                Text(
                                    text = kotItem.menu_item?.description ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    lineHeight = 14.sp
                                )
                            }
                            // Instructions
                            if (!kotItem.instructions.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Note: ${kotItem.instructions}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Price & Qty
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "x${kotItem.quantity ?: 0}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "₹${(kotItem.total_price ?: 0.0).toInt()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    if (index < kot.items.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 28.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VegNonVegIcon(isVegetarian: Boolean, modifier: Modifier = Modifier) {
    val color = if (isVegetarian) Color(0xFF4CAF50) else Color(0xFFE91E63)
    Box(
        modifier = modifier
            .size(16.dp)
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
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
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (status == selectedStatus),
                            onClick = { selectedStatus = status }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            style = MaterialTheme.typography.bodyLarge
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

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            val outputFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e2: Exception) {
            dateString
        }
    }
}
