package com.swadratna.swadratna_staff.ui.screens.tables

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_staff.ui.screens.orders.OrderManagementViewModel
import com.swadratna.swadratna_staff.ui.screens.orders.CustomerState
import com.swadratna.swadratna_staff.ui.screens.orders.OccupyTableState
import com.swadratna.swadratna_staff.ui.theme.AppGreen
import com.swadratna.swadratna_staff.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTableDialog(
    tableNumber: Int,
    onDismiss: () -> Unit,
    onTableOccupied: (String) -> Unit,
    orderManagementViewModel: OrderManagementViewModel = hiltViewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }

    val customerState by orderManagementViewModel.customerState.collectAsState()
    val occupyTableState by orderManagementViewModel.occupyTableState.collectAsState()

    val isLoading = customerState is CustomerState.Loading || occupyTableState is OccupyTableState.Loading

    LaunchedEffect(customerState) {
        if (customerState is CustomerState.Success) {
            val customer = (customerState as CustomerState.Success).customer
            orderManagementViewModel.occupyTable(tableNumber, customer.id)
        }
    }

    Dialog(onDismissRequest = {
        orderManagementViewModel.resetCustomerState()
        orderManagementViewModel.resetOccupyTableState()
        onDismiss()
    }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Assign Table $tableNumber", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val currentOccupyTableState = occupyTableState
                    when (currentOccupyTableState) {
                        is OccupyTableState.Success -> {
                            val orderId = currentOccupyTableState.response.order.id
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                Icon(Icons.Outlined.Done , contentDescription = "Success" , tint = AppGreen)
                                Text("Table assigned successfully. Order ID: $orderId",
                                    color = AppGreen
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                Button(onClick = {
                                    resetTableOccupancyLocalData(orderManagementViewModel)
                                    onTableOccupied(orderId.toString());
                                    onDismiss()
                                }) {
                                    Text("Go to Order")
                                }
                                Button(onClick = {
                                    resetTableOccupancyLocalData(orderManagementViewModel)
                                    onDismiss() }) {
                                    Text("Done")
                                }
                            }
                        }
                        is OccupyTableState.Error -> {
                            Text("Error: ${currentOccupyTableState.message}", color = MaterialTheme.colorScheme.error)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                Button(onClick = { orderManagementViewModel.resetOccupyTableState() }) {
                                    Text("Try Again")
                                }
                                Button(onClick = { onDismiss() }) {
                                    Text("Cancel")
                                }
                            }
                        }
                        else -> {
                            // Initial state or after an error reset
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
                                enabled = !isLoading
                            ) {
                                Text("Assign Table", color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun resetTableOccupancyLocalData(orderManagementViewModel: OrderManagementViewModel) {
    orderManagementViewModel.resetCustomerState()
    orderManagementViewModel.resetOccupyTableState()
}

@Composable
fun SuccessfullAssignTableCard() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Done , contentDescription = "Success" , tint = Color.White, modifier =
            Modifier.size(35.dp).clip(CircleShape).background(AppGreen).padding(8.dp))
        Text("Table assigned successfully. Order ID: ",
            color = AppGreen
        )
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {

        Button(onClick = {
//            onDismiss()
        }) {
            Text("Done")
        }

        Button(onClick = {
//            onTableOccupied(orderId.toString()); onDismiss()
        }) {
            Text("Move to Order")
        }

    }
}

@Preview
@Composable
fun CardPreview() {
    Column(
        modifier = Modifier
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Assign Table ", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        SuccessfullAssignTableCard()
    }
}