package com.swadratna.swadratna_staff.ui.screens.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.data.remote.model.Table
import com.swadratna.swadratna_staff.data.remote.model.Occupancy
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.screens.orders.OrderManagementViewModel
import com.swadratna.swadratna_staff.ui.screens.orders.TableListState
import com.swadratna.swadratna_staff.ui.components.SwipeRefreshContainer
import com.swadratna.swadratna_staff.ui.theme.Red80

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
                    val tables = (tableListState as TableListState.Success).tables.tables
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { showParcelDialog = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(text = "Parcel")
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
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
            Text(
                text = "${table.table_id}.",
                color = if(table.is_occupied)MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
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
