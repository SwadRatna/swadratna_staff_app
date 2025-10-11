package com.swadratna.swadratna_staff.ui.screens.orders

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.data.remote.model.BillDetail
import com.swadratna.swadratna_staff.data.remote.model.BillLineItem // Assuming this is your data class
import com.swadratna.swadratna_staff.data.remote.model.StaffRole
import com.swadratna.swadratna_staff.ui.components.SlideToConfirm
import com.swadratna.swadratna_staff.ui.theme.Red80
import com.swadratna.swadratna_staff.utils.BillPrinterUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun VegNonVegIndicator(isVeg: Boolean, modifier: Modifier = Modifier) {
    // NOTE: isVeg must be determined from your BillLineItem data structure.
    val color = if (isVeg) Color(0xFF4CAF50) else Color(0xFFD32F2F)
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .border(1.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun TipOptionChip(
    amount: Double? = null,
    text: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF42A5F5) else Color(0xFFF5F5F5)
    val textColor = if (isSelected) Color.White else Color.Black
    val borderColor = if (isSelected) Color(0xFF42A5F5) else Color.Transparent

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        modifier = Modifier
            .height(40.dp)
            .padding(horizontal = 4.dp)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text ?: "₹${"%.0f".format(amount)}",
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun BillDetailRow(label: String, amount: Double, prefix: String = "", modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Text(
            "$prefix₹${"%.1f".format(amount)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}


// =========================================================================================
// !!! ADAPTED MAIN SCREEN COMPOSABLE !!!
// =========================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayBillScreen(
    navController: NavController,
    orderId: String?,
    viewModel: OrderManagementViewModel = hiltViewModel()
) {
    val billDetailsState by viewModel.billDetailsState.collectAsStateWithLifecycle()
    val currentStaffUser by viewModel.currentStaffUser.collectAsStateWithLifecycle()
    val currentBill by viewModel.currentBill.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTipAmount by remember { mutableStateOf<Double?>(null) }
    var showCustomTipDialog by remember { mutableStateOf(false) }
    var customTipInput by remember { mutableStateOf("") }
    val role by viewModel.staffRole.collectAsStateWithLifecycle()

    val billDetail = (billDetailsState as? BillDetailsState.Success)?.billDetail

    // Default/Zero values for calculation until data loads
    val totalAmount = (billDetailsState as? BillDetailsState.Success)?.billDetail?.bill?.totalAmount ?: 0.0
    val totalPayable = totalAmount + (selectedTipAmount ?: 0.0)
    
    // Print bill function
    fun printBill() {
        billDetail?.let { bill ->
            coroutineScope.launch {
                try {
                    // First try Bluetooth printing
                    val bluetoothSuccess = withContext(Dispatchers.IO) {
                        BillPrinterUtil.printViaBluetooth(context, BillPrinterUtil.generateBillText(
                            billDetail = bill,
                            storeAddress = currentStaffUser?.location?.address,
                            storeName = "SWAD RATNA",
                            storePhone = currentStaffUser?.location?.location_mobile_number,
                            customerName = currentBill?.customerName,
                            customerMobile = null, // TODO: Get customer mobile number
                            cashierName = currentStaffUser?.username
                        ))
                    }
                    
                    if (bluetoothSuccess) {
                        Toast.makeText(context, "Bill printed successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Fallback to PDF generation
                        val pdfFile = withContext(Dispatchers.IO) {
                            BillPrinterUtil.generatePdfBill(
                                context = context,
                                billDetail = bill,
                                storeAddress = currentStaffUser?.location?.address,
                                storeName = "SWAD RATNA",
                                storePhone = currentStaffUser?.location?.location_mobile_number,
                                customerName = currentBill?.customerName,
                                customerMobile = null, // TODO: Get customer mobile number
                                cashierName = currentStaffUser?.username
                            )
                        }
                        
                        if (pdfFile != null) {
                            Toast.makeText(context, "Bill saved as PDF: ${pdfFile.name}", Toast.LENGTH_LONG).show()
                            
                            // Open PDF for preview
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.fromFile(pdfFile), "application/pdf")
                                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                            }
                            
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "PDF saved to Downloads folder", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Failed to generate bill", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error printing bill: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(context, "Bill details not available", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(orderId) {
        orderId?.let {
            viewModel.getBillDetails(it)
            viewModel.findBill(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay Bill", fontWeight = FontWeight.SemiBold) },
                actions = {
                    Button(
                        onClick = { printBill() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = billDetail != null
                    ) {
                        Text("🖨️ Print Bill", fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        bottomBar = {
            if(role == StaffRole.WAITER.roleName) {
                Button(
                    onClick = { /* TODO: Handle Make Payment */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    enabled = billDetailsState is BillDetailsState.Success // Enable only when data is loaded
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("₹${"%.0f".format(totalPayable)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Request Bill Approval", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "Make Payment", tint = Color.White)
                        }
                    }
                }
            } else if(role== StaffRole.MANAGER.roleName){
                SlideToConfirm(
                    text = "Slide to Approve Bill",
                    onConfirmation = {
                        billDetail?.bill?.id?.let {
                            viewModel.approveBill("ACCEPT" , "Looks fine" ,it)
                        }
                    },
                    trackColor = Red80,
                    thumbColor = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }

        }
    ) { paddingValues ->
        when (billDetailsState) {
            is BillDetailsState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is BillDetailsState.Success -> {
                val billDetail = (billDetailsState as BillDetailsState.Success).billDetail
                SuccessBillLayout(
                    billDetail = billDetail,
                    paddingValues = paddingValues,
                    totalPayable = totalPayable,
                    selectedTipAmount = selectedTipAmount,
                    onTipSelected = { selectedTipAmount = it },
                    onCustomTipClicked = { showCustomTipDialog = true }
                )
            }
            is BillDetailsState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${(billDetailsState as BillDetailsState.Error).message}", color = MaterialTheme.colorScheme.error)
                }
            }
            BillDetailsState.Idle -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(text = "Select an order to view bill details.")
                }
            }
        }
    }

    // Custom Tip Dialog
    if (showCustomTipDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTipDialog = false },
            title = { Text("Enter Custom Tip") },
            text = {
                OutlinedTextField(
                    value = customTipInput,
                    onValueChange = { customTipInput = it },
                    label = { Text("Tip Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    selectedTipAmount = customTipInput.toDoubleOrNull() ?: 0.0
                    showCustomTipDialog = false
                    customTipInput = ""
                }) {
                    Text("Add Tip")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTipDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SuccessBillLayout(
    billDetail: BillDetail, // Type-safe to your BillDetail class
    paddingValues: PaddingValues,
    totalPayable: Double,
    selectedTipAmount: Double?,
    onTipSelected: (Double?) -> Unit,
    onCustomTipClicked: () -> Unit
) {
    val bill = billDetail.bill
    val lineItems = billDetail.lineItems

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {

        // 2. My Orders Section (Line Items)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("My Orders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(lineItems) { item: BillLineItem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // NOTE: isVeg is not in your current data structure, assuming true
                    VegNonVegIndicator(isVeg = item.menuItem.isVegetarian, modifier = Modifier.padding(end = 8.dp))
                    Column {
                        Text(item.menuItem.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        item.instructions?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
                Text(
                    "${item.quantity} x ₹${"%.0f".format(item.price / item.quantity)}", // Calculate unit price
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        item {
            HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 8.dp))
        }


        item {
            Text("Your bill details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            BillDetailRow("Item total", bill?.totalAmount ?: 0.0) // Using total as item total placeholder
            BillDetailRow("CGST", 36.5, prefix = "+ ")
            BillDetailRow("SGST", 36.5, prefix = "+ ")

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.Black, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Amount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("₹${"%.0f".format(totalPayable)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}