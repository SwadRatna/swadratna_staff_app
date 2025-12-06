package com.swadratna.swadratna_staff.ui.screens.kot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.data.remote.model.KotItemX
import com.swadratna.swadratna_staff.ui.components.SwipeRefreshContainer
import com.swadratna.swadratna_staff.ui.components.NetworkTopSnackbarHost
import java.text.SimpleDateFormat
import java.util.*
import com.swadratna.swadratna_staff.R
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KotListScreen(
    navController: NavController,
    viewModel: KotViewModel = hiltViewModel()
) {
    val kotListState by viewModel.kotListState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val showOnlyPending by viewModel.showOnlyPending.collectAsState()

    val isOnline by viewModel.isOnline.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshKots()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            viewModel.refreshKots()
        }
    }

    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedKot by remember { mutableStateOf<KotItemX?>(null) }

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Kitchen Order Tickets") },
//                actions = {
//                    IconButton(
//                        onClick = { viewModel.setShowOnlyPending(!showOnlyPending) }
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.ic_filter),
//                            contentDescription = "Filter",
//                            tint = if (showOnlyPending) MaterialTheme.colorScheme.primary
//                                  else MaterialTheme.colorScheme.onSurface
//                        )
//                    }
//                }
//            )
//        }
    ) { paddingValues ->
        SwipeRefreshContainer(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshKots() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NetworkTopSnackbarHost(
                    isOnline = isOnline,
                    onRefresh = { viewModel.refreshKots() },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                when (kotListState) {
                    is KotListState.Loading -> {
                        if (!isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    is KotListState.Error -> {
                        if (isOnline) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = (kotListState as KotListState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.loadKots() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is KotListState.Empty -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (showOnlyPending) "No pending KOTs" else "No KOTs found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pull down to refresh",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                    is KotListState.Success -> {
                        val kots = (kotListState as KotListState.Success).kots
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(kots, key = { it.id }) { kot ->
                                KotCard(
                                    kot = kot,
                                    onClick = { 
                                        selectedKot = kot
                                        showStatusDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(isOnline) {
        if (isOnline) {
            viewModel.refreshKots()
        }
    }

    // Status Update Dialog
    if (showStatusDialog && selectedKot != null) {
        KotStatusUpdateDialog(
            kot = selectedKot!!,
            onDismiss = { 
                showStatusDialog = false
                selectedKot = null
            },
            onStatusUpdate = { newStatus ->
                viewModel.updateKotStatus(selectedKot!!.id, newStatus)
                showStatusDialog = false
                selectedKot = null
            }
        )
    }
}

@Composable
fun KotCard(
    kot: KotItemX,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
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
                        text = "KOT #${kot.kot_number}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Table ${kot.table.table_id} • Order #${kot.order_id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Status Chip
                KotStatusChip(status = kot.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items Summary
            if (!kot.items.isNullOrEmpty()) {
                Text(
                    text = "${kot.items.size} item${if (kot.items.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                // Show first few items
                val displayItems = kot.items.take(3)
                displayItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${item.menu_item?.name ?: "Unknown Item"}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "x${item.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (kot.items.size > 3) {
                    Text(
                        text = "+${kot.items.size - 3} more items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp
            Text(
                text = formatKotTimestamp(kot.created_at),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun KotStatusChip(status: String) {
    val (backgroundColor, contentColor) = when (status.lowercase()) {
        "pending" -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        "preparing" -> Pair(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        "ready" -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        "served" -> Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
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

private fun formatKotTimestamp(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(timestamp)
        
        val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        timestamp // Return original if parsing fails
    }
}
