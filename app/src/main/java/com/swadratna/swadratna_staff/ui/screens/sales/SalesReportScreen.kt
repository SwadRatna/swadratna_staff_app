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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sale Report", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
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
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true }
                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                .padding(12.dp),
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
                                    .padding(12.dp)
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
                                    .padding(12.dp)
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
                                .weight(1f)
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
                    state?.data?.sales?.let {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(it) { sale ->
                                SaleItemCard(sale)
                            }
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
                            text = if (sale.orderType.isNullOrEmpty()) "Order #${sale.id}" else sale.orderType,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Right: Bill No | Date
                Text(
                    text = "${sale.billNumber} | ${formatDate(sale.createdAt)}",
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
                                if (!sale.paymentMode.isNullOrEmpty()) {
                                    append(" | ")
                                    append(sale.paymentMode)
                                }
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
//                Column(horizontalAlignment = Alignment.End) {
//                    Icon(
//                        imageVector = Icons.Default.Check,
//                        contentDescription = "Paid",
//                        tint = Color(0xFF008000),
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    OutlinedButton(
//                        onClick = { /* Sale Return */ },
//                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
//                        modifier = Modifier.height(32.dp),
//                        shape = RoundedCornerShape(4.dp)
//                    ) {
//                        Text("Sale Return", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
//                    }
//                }
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
