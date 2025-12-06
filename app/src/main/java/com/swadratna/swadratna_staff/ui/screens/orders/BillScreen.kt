package com.swadratna.swadratna_staff.ui.screens.orders

import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.data.remote.model.BILL_STATUS
import com.swadratna.swadratna_staff.data.remote.model.BillDetail
import com.swadratna.swadratna_staff.data.remote.model.StaffRole
import com.swadratna.swadratna_staff.utils.BillPrinterUtil
import com.swadratna.swadratna_staff.utils.rememberBluetoothPermissionLauncher
import com.swadratna.swadratna_staff.ui.components.NetworkTopSnackbarHost
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun VegNonVegIndicator(isVeg: Boolean, modifier: Modifier = Modifier) {
    val color = if (isVeg) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Box(
        modifier = modifier
            .size(14.dp)
            .border(1.dp, color, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun InfoColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

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
    val role by viewModel.staffRole.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentMode by remember { mutableStateOf("cash") }
    var transactionId by remember { mutableStateOf("") }
    var paymentNotes by remember { mutableStateOf("") }
    var amountPaidInput by remember { mutableStateOf("") }
    
    // Post Payment Dialog State
    var showPostPaymentDialog by remember { mutableStateOf(false) }
    var postPaymentPhoneNumber by remember { mutableStateOf<String?>(null) }
    var postPaymentAmount by remember { mutableStateOf(0.0) }

    val billDetail = (billDetailsState as? BillDetailsState.Success)?.billDetail
    
    // Handle Payment Success
    LaunchedEffect(Unit) {
        viewModel.paymentSuccess.collect { response ->
            showPostPaymentDialog = true
            postPaymentPhoneNumber = response.invoice?.customer_phone
            postPaymentAmount = response.bill.total_amount
        }
    }

    val shareBill = remember(billDetail, currentStaffUser, currentBill) {
        { phoneNumber: String? ->
            billDetail?.let { bill ->
                // Generate Text
                val sb = StringBuilder()
                sb.append("*SWAD RATNA*\n")
                currentStaffUser?.location?.address?.let { addr ->
                    if (addr.locality.isNotBlank()) sb.append("${addr.locality}, ")
                    if (addr.city.isNotBlank()) sb.append("${addr.city}")
                    sb.append("\n")
                }
                sb.append("\n")
                sb.append("Bill No: ${bill.bill.billNumber}\n")
                val date = try {
                    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    sdf.format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(bill.bill.createdAt) ?: java.util.Date())
                } catch (e: Exception) { bill.bill.createdAt }
                sb.append("Date: $date\n")

                if (!currentBill?.customerName.isNullOrBlank()) {
                    sb.append("Customer: ${currentBill?.customerName}\n")
                }
                
                sb.append("\n*Items:*\n")
                bill.lineItems.forEach { item ->
                    sb.append("${item.menuItem.name} x ${item.quantity} = ₹${"%.2f".format(item.totalPrice)}\n")
                }
                
                sb.append("\n")
                sb.append("Subtotal: ₹${"%.2f".format(bill.bill.subTotal)}\n")
                if (bill.bill.taxAmount > 0) sb.append("Tax: ₹${"%.2f".format(bill.bill.taxAmount)}\n")
                if (bill.bill.discountAmount > 0) sb.append("Discount: -₹${"%.2f".format(bill.bill.discountAmount)}\n")
                sb.append("*Grand Total: ₹${"%.2f".format(bill.bill.totalAmount)}*\n")
                
                sb.append("\nThank you for dining with Swad Ratna! Visit us again!\n")

                val message = sb.toString()

                if (!phoneNumber.isNullOrBlank()) {
                     try {
                        val cleanPhone = phoneNumber.filter { it.isDigit() }
                        val finalPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                        
                        val url = "https://api.whatsapp.com/send?phone=$finalPhone&text=${Uri.encode(message)}"
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(url)
                            setPackage("com.whatsapp")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "WhatsApp not installed or error opening", Toast.LENGTH_SHORT).show()
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, message)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Bill"))
                    }
                } else {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Bill"))
                }
            }
        }
    }

    // Print bill function
    val performPrint = rememberBluetoothPermissionLauncher {
        billDetail?.let { bill ->
            val billText = BillPrinterUtil.generateBillText(
                billDetail = bill,
                storeAddress = currentStaffUser?.location?.address,
                storeName = "SWAD RATNA",
                storePhone = currentStaffUser?.location?.location_mobile_number,
                customerName = currentBill?.customerName,
                customerMobile = null, 
                cashierName = currentStaffUser?.username
            )
            
            BillPrinterUtil.printWithChooser(context, billText) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Bill Details",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { performPrint() }, enabled = billDetail != null) {
                        Text("🖨️", fontSize = 20.sp)
                    }
                    IconButton(onClick = { shareBill(null) }, enabled = billDetail != null) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (billDetail != null) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (role == StaffRole.MANAGER.roleName) {
                            val status = billDetail.bill.status.lowercase()
                            val isAccepted = status == BILL_STATUS.ACCEPTED.value.lowercase()
                            val isPending = status == BILL_STATUS.PENDING.value.lowercase()
                            val isHold = status == BILL_STATUS.Hold.value.lowercase()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isPending || isHold) {
                                    Button(
                                        onClick = { viewModel.approveBill("HOLD", "", billDetail.bill.id, orderId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                        modifier = Modifier.weight(1f),
                                        enabled = !loading
                                    ) {
                                        Text("Hold")
                                    }
                                    Button(
                                        onClick = { viewModel.approveBill("REJECT", "", billDetail.bill.id, orderId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(1f),
                                        enabled = !loading
                                    ) {
                                        Text("Reject")
                                    }
                                    Button(
                                        onClick = { viewModel.approveBill("ACCEPT", "", billDetail.bill.id, orderId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.weight(1f),
                                        enabled = !loading
                                    ) {
                                        Text("Accept")
                                    }
                                } else {
                                    // If already accepted or paid or rejected (but mainly if accepted)
                                    Button(
                                        onClick = {
                                            amountPaidInput = "${billDetail.bill.totalAmount}"
                                            showPaymentDialog = true
                                        },
                                        enabled = isAccepted && !loading, // Enable only if accepted and not loading
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        if (loading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        } else {
                                            Text(if(status == BILL_STATUS.PAID.value.lowercase()) "Bill Paid" else "Mark as Paid")
                                        }
                                    }
                                }
                            }
                        } else if (role == StaffRole.WAITER.roleName) {
                            // Waiter view
                            val status = billDetail.bill.status
                            Button(
                                onClick = { /* Request approval logic if needed */ },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false
                            ) {
                                Text("Status: $status")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (billDetailsState is BillDetailsState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (billDetail != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Badge
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Surface(
                                color = when(billDetail.bill.status.lowercase()) {
                                    BILL_STATUS.PAID.value.lowercase() -> MaterialTheme.colorScheme.primaryContainer
                                    BILL_STATUS.REJECTED.value.lowercase() -> MaterialTheme.colorScheme.errorContainer
                                    BILL_STATUS.ACCEPTED.value.lowercase() -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = billDetail.bill.status.uppercase(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = when(billDetail.bill.status.lowercase()) {
                                        BILL_STATUS.PAID.value.lowercase() -> MaterialTheme.colorScheme.onPrimaryContainer
                                        BILL_STATUS.REJECTED.value.lowercase() -> MaterialTheme.colorScheme.onErrorContainer
                                        BILL_STATUS.ACCEPTED.value.lowercase() -> MaterialTheme.colorScheme.onSecondaryContainer
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Info Grid
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    InfoColumn("Bill No", "#${billDetail.bill.billNumber}")
                                    InfoColumn("Table", "${billDetail.bill.tableId}")
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    val date = try {
                                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                                        val parsed = sdf.parse(billDetail.bill.createdAt)
                                        val out = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                                        out.format(parsed ?: java.util.Date())
                                    } catch (e: Exception) {
                                        billDetail.bill.createdAt
                                    }
                                    InfoColumn("Date", date)
                                    InfoColumn("Server", currentStaffUser?.username ?: "Unknown")
                                }
                            }
                        }
                    }

                    item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }

                    // Items List Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Item", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Qty", modifier = Modifier.weight(0.15f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            Text("Price", modifier = Modifier.weight(0.15f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                            Text("Amount", modifier = Modifier.weight(0.2f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                        }
                    }

                    items(billDetail.lineItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(0.5f), verticalAlignment = Alignment.CenterVertically) {
                                VegNonVegIndicator(isVeg = item.menuItem.isVegetarian)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.menuItem.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${item.quantity}",
                                modifier = Modifier.weight(0.15f),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "₹${"%.0f".format(item.price)}",
                                modifier = Modifier.weight(0.15f),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "₹${"%.0f".format(item.totalPrice)}",
                                modifier = Modifier.weight(0.2f),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }

                    // Summary
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtotal",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium)
                                    Text("₹${"%.2f".format(billDetail.bill.subTotal)}",
                                        color = MaterialTheme.colorScheme.onSurface
                                        , style = MaterialTheme.typography.bodyMedium)
                                }
                                if (billDetail.bill.discountAmount > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Discount",
                                            color = MaterialTheme.colorScheme.onSurface
                                            , style = MaterialTheme.typography.bodyMedium)
                                        Text("-₹${"%.2f".format(billDetail.bill.discountAmount)}",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                if (billDetail.bill.taxAmount > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Taxes",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium)
                                        Text("₹${"%.2f".format(billDetail.bill.taxAmount)}",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Grand Total",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("₹${"%.2f".format(billDetail.bill.totalAmount)}",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    // Bottom Spacer
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            } else if (billDetailsState is BillDetailsState.Error) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error loading bill", color = MaterialTheme.colorScheme.error)
                }
            }
            NetworkTopSnackbarHost(
                isOnline = isOnline,
                onRefresh = { orderId?.let { viewModel.getBillDetails(it) } },
                modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
            )
        }
    }

    // Payment Dialog
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Record Payment") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Payment Mode", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("cash", "card", "upi").forEach { mode ->
                            val selected = paymentMode == mode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .height(36.dp)
                                    .clickable { paymentMode = mode }
                                    .border(
                                        1.dp,
                                        if (selected) Color.Transparent else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                    Text(mode.uppercase(), color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text("Transaction ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountPaidInput,
                        onValueChange = { amountPaidInput = it },
                        label = { Text("Amount Paid") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = amountPaidInput.toDoubleOrNull()
                    val current = billDetail
                    if (amount != null && current != null) {
                        val request = com.swadratna.swadratna_staff.data.remote.services.RecordPaymentRequest(
                            payment_mode = paymentMode,
                            transaction_id = if (transactionId.isBlank()) null else transactionId,
                            notes = if (paymentNotes.isBlank()) null else paymentNotes,
                            amount_paid = amount
                        )
                        viewModel.recordBillPayment(current.bill.id, request, orderId)
                        showPaymentDialog = false
                    } else {
                        Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Post Payment Success Dialog
    if (showPostPaymentDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismissal without action */ },
            title = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Bill Paid Successfully")
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Amount: ₹${"%.2f".format(postPaymentAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (!postPaymentPhoneNumber.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Share receipt with customer?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!postPaymentPhoneNumber.isNullOrBlank()) {
                        Button(
                            onClick = {
                                shareBill(postPaymentPhoneNumber)
                                showPostPaymentDialog = false
                                navController.navigate(com.swadratna.swadratna_staff.navigation.NavigationRoute.Tables.route) {
                                    popUpTo(com.swadratna.swadratna_staff.navigation.NavigationRoute.Tables.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // Keeping WhatsApp color as brand color
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Receipt")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    OutlinedButton(
                        onClick = {
                            showPostPaymentDialog = false
                            navController.navigate(com.swadratna.swadratna_staff.navigation.NavigationRoute.Tables.route) {
                                popUpTo(com.swadratna.swadratna_staff.navigation.NavigationRoute.Tables.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        )
    }
}
