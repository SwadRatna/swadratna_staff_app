package com.swadratna.swadratna_staff.ui.screens.tables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_staff.ui.screens.orders.OrderManagementViewModel
import com.swadratna.swadratna_staff.ui.screens.orders.CustomerState
import com.swadratna.swadratna_staff.ui.screens.orders.OccupyTableState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTableDialog(
    tableNumber: Int,
    onDismiss: () -> Unit,
    onTableOccupied: (String) -> Unit, // New callback
    orderManagementViewModel: OrderManagementViewModel = hiltViewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }

    val customerState by orderManagementViewModel.customerState.collectAsState()
    val occupyTableState by orderManagementViewModel.occupyTableState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(customerState) {
        when (customerState) {
            is CustomerState.Success -> {
                val customer = (customerState as CustomerState.Success).customer
                orderManagementViewModel.occupyTable(tableNumber, customer.id)
                orderManagementViewModel.resetCustomerState()
            }
            is CustomerState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (customerState as CustomerState.Error).message ?: "Unknow Error",
                    actionLabel = "Dismiss"
                )
                orderManagementViewModel.resetCustomerState()
            }
            else -> {}
        }
    }

    LaunchedEffect(occupyTableState) {
        when (occupyTableState) {
            is OccupyTableState.Success -> {
                val orderId = (occupyTableState as OccupyTableState.Success).response.order.id
                snackbarHostState.showSnackbar(
                    message = "Table ${tableNumber} assigned successfully. Order ID: $orderId",
                    actionLabel = "Dismiss"
                )
                orderManagementViewModel.resetOccupyTableState()
                onDismiss()
                onTableOccupied(orderId.toString()) // Call the new callback
            }
            is OccupyTableState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (occupyTableState as OccupyTableState.Error).message,
                    actionLabel = "Dismiss"
                )
                orderManagementViewModel.resetOccupyTableState()
            }
            else -> {}
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            SnackbarHost(hostState = snackbarHostState)
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Assign Table $tableNumber", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                
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
                    placeholder = { Text("e.g. +1 (555) 123-4567 or alice@example.com") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = "Table $tableNumber",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Table Assignment") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (fullName.isNotBlank() && contactInfo.isNotBlank()) {
                            orderManagementViewModel.getOrCreateCustomer(contactInfo, fullName)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = customerState !is CustomerState.Loading && occupyTableState !is OccupyTableState.Loading
                ) {
                    if (customerState is CustomerState.Loading || occupyTableState is OccupyTableState.Loading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Assign Table", color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}