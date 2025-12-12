package com.swadratna.swadratna_staff.ui.screens.sales

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.data.remote.model.SaleTransaction
import java.text.SimpleDateFormat
import java.util.*

import com.swadratna.swadratna_staff.ui.components.NetworkTopSnackbarHost

import com.swadratna.swadratna_staff.ui.components.SwipeRefreshContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportScreen(
    navController: NavController,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val fromDate by viewModel.fromDate.collectAsState()
    val toDate by viewModel.toDate.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    val context = LocalContext.current
    
    LaunchedEffect(isOnline) {
        if (isOnline) {
            viewModel.refresh()
        }
    }

    val calendar = Calendar.getInstance()
    
    var expanded by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf("single") } // "single", "from", "to"

    // Date Picker Logic
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateStr = format.format(cal.time)
                
                when (datePickerTarget) {
                    "single" -> viewModel.setDateFilter(dateStr)
                    "from" -> viewModel.setFromDate(dateStr)
                    "to" -> viewModel.setToDate(dateStr)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    SwipeRefreshContainer(
        isRefreshing = uiState is SalesUiState.Loading,
        onRefresh = { viewModel.refresh() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Filter Section
                Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Date Type Selector (Today/Custom)
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true }
                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val label = when {
                                selectedDate != null -> "Single Date"
                                fromDate != null || toDate != null -> "Date Range"
                                else -> "Lifetime"
                            }
                            Text(label) 
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Today") },
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    viewModel.setDateFilter(format.format(cal.time))
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Yesterday") },
                                onClick = {
                                    viewModel.setYesterdayFilter()
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Tomorrow") },
                                onClick = {
                                    viewModel.setTomorrowFilter()
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("This Week") },
                                onClick = {
                                    viewModel.setThisWeekFilter()
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("This Month") },
                                onClick = {
                                    viewModel.setThisMonthFilter()
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("This Year") },
                                onClick = {
                                    viewModel.setThisYearFilter()
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Lifetime") },
                                onClick = {
                                    viewModel.setLifetimeFilter()
                                    expanded = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date Display
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (selectedDate != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                    .clickable { 
                                        datePickerTarget = "single"
                                        datePickerDialog.show() 
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = selectedDate ?: "Select Date",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                    .clickable { 
                                        datePickerTarget = "from"
                                        datePickerDialog.show() 
                                    }
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = fromDate ?: "From Date",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                    .clickable { 
                                        datePickerTarget = "to"
                                        datePickerDialog.show() 
                                    }
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = toDate ?: "To Date",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        }
                    }
                }
            }

            // Summary Section
            when (val state = uiState) {
                is SalesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is SalesUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is SalesUiState.Success -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        // Amount Card
                        Card(
                            modifier = Modifier
                                .weight(1.2f)
                                .padding(end = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(4.dp),
                             border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Amount", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    "₹${state.data.summary.totalAmount}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                // Green bar at bottom
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(Color.Green)
                                )
                            }
                        }

                        // Count Card
                        Card(
                            modifier = Modifier
                                .weight(0.8f)
                                .padding(start = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(4.dp),
                             border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                ) {
                                    Text("Count", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        "${state.data.summary.count}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                     Spacer(modifier = Modifier.height(4.dp))
                                     Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(Color.Blue)
                                )
                                }
                                // Filter Icon/Button placeholder
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .fillMaxHeight()
                                        .clickable { /* Filter */ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = com.swadratna.swadratna_staff.R.drawable.ic_filter), // Make sure this exists or use vector
                                        contentDescription = "Filter",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // List
                    state?.data?.sales?.let {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(it) { sale ->
                                SaleItemCard(sale) {
                                    navController.navigate(
                                        com.swadratna.swadratna_staff.navigation.NavigationRoute.Bill.createRoute(
                                            billId = sale.id.toString()
                                        )
                                    )
                                }
                            }
                        }
                    }

                }
            }
            }

            NetworkTopSnackbarHost(
                isOnline = isOnline,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun SaleItemCard(sale: SaleTransaction, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bill #${sale.billNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = buildString {
                            append(sale.orderType ?: "Order")
                            if (!sale.paymentMode.isNullOrEmpty()) {
                                append(" • ")
                                append(sale.paymentMode)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Status Chip
                SaleStatusChip(status = sale.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            Text(
                text = "₹${sale.amount}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                if (sale.editedBy != null) {
                    Text(
                        text = "Edited by ${sale.editedBy} on ${formatDate(sale.editedAt ?: "")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Timestamp
                Text(
                    text = formatDate(sale.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

        }
    }
}

@Composable
fun SaleStatusChip(status: String) {
    val (backgroundColor, contentColor) = when (status.lowercase()) {
        "paid" -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        "pending" -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        "cancelled" -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            fontSize = 11.sp
        )
    }
}

private fun formatDate(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(timestamp)
        
        val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        // Fallback for different format or error
        try {
             // Try another common format just in case, or just return original
             timestamp
        } catch (e2: Exception) {
            timestamp
        }
    }
}
