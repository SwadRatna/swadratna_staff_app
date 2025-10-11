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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.data.remote.model.Table
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.screens.orders.OrderManagementViewModel
import com.swadratna.swadratna_staff.ui.screens.orders.TableListState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.swadratna.swadratna_staff.ui.components.SwipeRefreshContainer
import com.swadratna.swadratna_staff.ui.theme.Orange80
import com.swadratna.swadratna_staff.ui.theme.Red40
import com.swadratna.swadratna_staff.ui.theme.Red80
import com.swadratna.swadratna_staff.ui.theme.RedGrey20
import com.swadratna.swadratna_staff.ui.theme.RedGrey80


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(navController: NavController,
                 orderManagementViewModel: OrderManagementViewModel = hiltViewModel()
) {
    val tableListState by orderManagementViewModel.tableListState.collectAsState()
    val isRefreshing by orderManagementViewModel.isRefreshing.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedTable by remember { mutableStateOf<Table?>(null) }

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

            when (tableListState) {
                is TableListState.Loading -> {
                    // Only show loading indicator if not refreshing
                    if (!isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                is TableListState.Error -> {
                    Text(text = (tableListState as TableListState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                is TableListState.Success -> {
                    val tables = (tableListState as TableListState.Success).tables.tables // Access the list of tables
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(tables) { table ->
                            TableCard(
                                table = table, onClick = {
                                    if (!table.is_occupied) {
                                        selectedTable = table
                                        showDialog = true
                                    } else {
                                        navController.navigate("${NavigationRoute.OrderTaking.route}/${table.id}/${table.occupancy.order_id}")
                                    }
                                })
                        }
                    }
                }
            }

            if (showDialog && selectedTable != null) {
                AssignTableDialog(
                    tableNumber = selectedTable!!.id,
                    onDismiss = { showDialog = false },
                    onTableOccupied = { orderId ->
                        showDialog = false // Dismiss the dialog
                        navController.navigate("${NavigationRoute.OrderTaking.route}/${selectedTable!!.id}/${orderId}")
                    }
                )
            }
        }
    }
}

@Composable
fun TableCard(table: Table, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .height(100.dp)
            .clickable(onClick = onClick)
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(if (table.is_occupied) Red80 else MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
            verticalArrangement = if(table.is_occupied) Arrangement.SpaceAround else Arrangement.Top
        ) {
            Text(
                text = "${table.table_id}.",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            if (table.is_occupied) {
                Text(
                    text = "${table.occupancy.user_name}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
