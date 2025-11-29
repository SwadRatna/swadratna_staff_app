package com.swadratna.swadratna_staff.ui.screens.sales

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Date Picker Logic
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                viewModel.setDateFilter(format.format(cal.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sale List", fontWeight = FontWeight.Bold)
                        Text(
                            "FAST v39.0 | 7906897228 | 1913", // Placeholder/Hardcoded from screenshot
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { /* Visualize */ },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("VISUALIZE")
                }
                Button(
                    onClick = { /* New Sale */ },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("NEW SALE")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Show selection dialog */ }
                            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (selectedDate != null) "Today" else "Custom Range") // Simplified logic
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date Display
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                .clickable { datePickerDialog.show() }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = selectedDate ?: fromDate ?: "Start Date",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                        
                        if (selectedDate == null) { // Show second date box only for range
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = toDate ?: "End Date",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        } else {
                             Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = selectedDate ?: "",
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
                            .padding(horizontal = 8.dp)
                    ) {
                        // Amount Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(4.dp),
                             border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Amount", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    "₹${state.data.totalAmount}",
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
                                .weight(1f)
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
                                        "${state.data.totalCount}",
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
                                        .background(Color.LightGray)
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.data.sales) { sale ->
                            SaleItemCard(sale)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaleItemCard(sale: SaleTransaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Table/Order Type
                Column {
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = sale.tableName ?: sale.orderType ?: "Order #${sale.id}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Right: Bill No | Date
                Text(
                    text = "${sale.billNumber} | ${formatDate(sale.date)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Amount | Status
                Column {
                    Text(
                        text = buildString {
                            append("₹${sale.amount}")
                            append(" | ")
                            append(sale.status)
                        },
                        color = Color(0xFF008000), // Green
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    if (sale.editedBy != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Edited by ${sale.editedBy} on ${formatDate(sale.editedAt ?: "")}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Actions
                Column(horizontalAlignment = Alignment.End) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Paid",
                        tint = Color(0xFF008000),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { /* Sale Return */ },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Sale Return", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()) // Adjust format as needed
        val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return dateString
        outputFormat.format(date)
    } catch (e: Exception) {
        try {
             // Fallback for simpler format
             val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
             val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
             val date = inputFormat.parse(dateString) ?: return dateString
             outputFormat.format(date)
        } catch (e2: Exception) {
            dateString
        }
    }
}
