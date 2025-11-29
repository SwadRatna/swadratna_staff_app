package com.swadratna.swadratna_staff.ui.screens.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.data.remote.model.Table
import com.swadratna.swadratna_staff.data.remote.model.Occupancy
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.screens.orders.OrderManagementViewModel
import com.swadratna.swadratna_staff.ui.screens.orders.TableListState
import com.swadratna.swadratna_staff.ui.components.SwipeRefreshContainer
import com.swadratna.swadratna_staff.ui.theme.Red80
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(navController: NavController,
                 orderManagementViewModel: OrderManagementViewModel = hiltViewModel()
) {
    val tableListState by orderManagementViewModel.tableListState.collectAsState()
    val isRefreshing by orderManagementViewModel.isRefreshing.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedTable by remember { mutableStateOf<Table?>(null) }
    var showParcelDialog by remember { mutableStateOf(false) }

    val staffLocationId by orderManagementViewModel.staffLocationId.collectAsState()

    SwipeRefreshContainer(
        isRefreshing = isRefreshing,
        onRefresh = {
            staffLocationId?.let {
                orderManagementViewModel.getTables(it)
            }
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)) {

            val createParcelOrderState by orderManagementViewModel.createParcelOrderState.collectAsState()

            

            when (tableListState) {
                is TableListState.Loading -> {
                    if (!isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                is TableListState.Error -> {
                    Text(text = (tableListState as TableListState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                is TableListState.Success -> {
                    val response = (tableListState as TableListState.Success).tables
                    val tables = response.tables
                    val parcelOrders = response.parcel_orders

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Create Parcel Button
                        item(span = { GridItemSpan(3) }) {
                            Button(
                                onClick = { showParcelDialog = true },
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text(text = "Create Parcel")
                            }
                        }

                        // Active Parcels
                        if (parcelOrders.isNotEmpty()) {
                            item(span = { GridItemSpan(3) }) {
                                Text(
                                    text = "Active Parcels",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(parcelOrders, span = { GridItemSpan(3) }) { parcel ->
                                Button(
                                    onClick = {
                                        navController.navigate(
                                            NavigationRoute.OrderTaking.createRoute(
                                                tableNumber = 0,
                                                orderId = parcel.id,
                                                showMenuTab = true,
                                                showOrdersTab = true,
                                                defaultTab = 0
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text(text = "${parcel.customer_name ?: "Unknown"} - #${parcel.id}")
                                    if (parcel.last_non_served_kot_time != null) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        DelayTimer(startTime = parcel.last_non_served_kot_time)
                                    }
                                }
                            }
                        }

                        // Tables Header
                        item(span = { GridItemSpan(3) }) {
                            Text(
                                text = "Tables",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        // Tables Grid
                        items(tables, key = { it.id }) { table ->
                            TableCard(
                                table = table,
                                onClick = { clickedTable ->
                                    if (!clickedTable.is_occupied) {
                                        selectedTable = clickedTable
                                        showDialog = true
                                    } else {
                                        navController.navigate(
                                            NavigationRoute.OrderTaking.createRoute(
                                                tableNumber = clickedTable.id,
                                                orderId = clickedTable.occupancy.order_id,
                                                showMenuTab = true,
                                                showOrdersTab = true,
                                                defaultTab = 0
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (showDialog && selectedTable != null) {
                AssignTableDialog(
                    tableId = selectedTable!!.id,
                    tableNumber = selectedTable!!.table_id,
                    onDismiss = { showDialog = false },
                    onTableOccupied = { orderId ->
                        showDialog = false // Dismiss the dialog

                        navController.navigate(NavigationRoute.OrderTaking.createRoute(
                            tableNumber = selectedTable!!.id.toInt(),
                            orderId = orderId.toInt(),
                            showMenuTab = true,
                            showOrdersTab = true,
                            defaultTab = 0
                        ))
                    }
                )
            }

            if (showParcelDialog) {
                Dialog(onDismissRequest = {
                    orderManagementViewModel.resetCreateParcelOrderState()
                    showParcelDialog = false
                }) {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        var fullName by remember { mutableStateOf("") }
                        var contactInfo by remember { mutableStateOf("") }
                        val isLoading = createParcelOrderState is com.swadratna.swadratna_staff.ui.screens.orders.CreateParcelOrderState.Loading
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Create Parcel")
                            when (createParcelOrderState) {
                                is com.swadratna.swadratna_staff.ui.screens.orders.CreateParcelOrderState.Success -> {
                                    val orderId = (createParcelOrderState as com.swadratna.swadratna_staff.ui.screens.orders.CreateParcelOrderState.Success).response.order.id
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                        Button(onClick = {
                                            orderManagementViewModel.resetCreateParcelOrderState()
                                            showParcelDialog = false
                                            navController.navigate(
                                                NavigationRoute.OrderTaking.createRoute(
                                                    tableNumber = 0,
                                                    orderId = orderId,
                                                    showMenuTab = true,
                                                    showOrdersTab = true,
                                                    defaultTab = 0
                                                )
                                            )
                                        }) { Text("Go to Order") }
                                        Button(onClick = {
                                            orderManagementViewModel.resetCreateParcelOrderState()
                                            showParcelDialog = false
                                        }) { Text("Done") }
                                    }
                                }
                                is com.swadratna.swadratna_staff.ui.screens.orders.CreateParcelOrderState.Error -> {
                                    val msg = (createParcelOrderState as com.swadratna.swadratna_staff.ui.screens.orders.CreateParcelOrderState.Error).message
                                    Text(msg, color = MaterialTheme.colorScheme.error)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                        Button(onClick = { orderManagementViewModel.resetCreateParcelOrderState() }) { Text("Try Again") }
                                        Button(onClick = { showParcelDialog = false }) { Text("Cancel") }
                                    }
                                }
                                else -> {
                                    OutlinedTextField(
                                        value = fullName,
                                        onValueChange = { fullName = it },
                                        label = { Text("User's Full Name") },
                                        placeholder = { Text("e.g. Alice Smith") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = contactInfo,
                                        onValueChange = { contactInfo = it },
                                        label = { Text("Contact Information") },
                                        placeholder = { Text("e.g. +91 123843-4567 ") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            val loc = staffLocationId ?: 0
                                            if (fullName.isNotBlank()) {
                                                orderManagementViewModel.createParcelOrder(loc, fullName, contactInfo)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        enabled = !isLoading
                                    ) { Text("Create Parcel") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableCard(table: Table, onClick: (Table) -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .height(100.dp)
            .clickable(onClick = {onClick(table)})
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(if (table.is_occupied) Red80 else MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            verticalArrangement = if(table.is_occupied) Arrangement.SpaceAround else Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${table.table_id}.",
                    color = if(table.is_occupied)MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                if (table.is_occupied && table.last_non_served_kot_time != null) {
                    DelayTimer(
                        startTime = table.last_non_served_kot_time,
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            if (table.is_occupied) {
                Text(
                    text = "${table.occupancy.user_name}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DelayTimer(startTime: String, modifier: Modifier = Modifier, textColor: Color = Color.Red) {
    var timeText by remember { mutableStateOf("00:00") }

    LaunchedEffect(startTime) {
        while (true) {
            val now = System.currentTimeMillis()
            var startMillis = 0L
            
            try {
                // Try parsing with milliseconds first
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                startMillis = sdf.parse(startTime)?.time ?: now
            } catch (e: Exception) {
                try {
                    // Fallback to no milliseconds
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    startMillis = sdf.parse(startTime)?.time ?: now
                } catch (e2: Exception) {
                    startMillis = now
                }
            }

            val diff = now - startMillis

            if (diff > 0) {
                val seconds = (diff / 1000) % 60
                val minutes = (diff / (1000 * 60)) % 60
                val hours = (diff / (1000 * 60 * 60))

                timeText = if (hours > 0) {
                    String.format("%02d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format("%02d:%02d", minutes, seconds)
                }
            } else {
                timeText = "00:00"
            }
            delay(1000)
        }
    }

    Text(
        text = timeText,
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}
