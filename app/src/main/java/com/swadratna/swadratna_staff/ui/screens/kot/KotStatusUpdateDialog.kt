package com.swadratna.swadratna_staff.ui.screens.kot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.swadratna.swadratna_staff.data.remote.model.KotItemX

@Composable
fun KotStatusUpdateDialog(
    kot: KotItemX,
    onDismiss: () -> Unit,
    onStatusUpdate: (String) -> Unit
) {
    val possibleStatuses = listOf("pending", "preparing", "ready", "served", "cancelled")
    val currentStatusIndex = possibleStatuses.indexOf(kot.status.lowercase())
    var selectedStatus by remember { mutableStateOf(kot.status.lowercase()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Update KOT Status",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "KOT #${kot.kot_number}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Current Status Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Current Status",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        KotStatusChip(status = kot.status)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Status Options
                Text(
                    text = "Select New Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    possibleStatuses.forEach { status ->
                        val isSelected = selectedStatus == status
                        val isCurrentStatus = status == kot.status.lowercase()
                        
                        StatusOptionCard(
                            status = status,
                            isSelected = isSelected,
                            isCurrentStatus = isCurrentStatus,
                            onClick = { selectedStatus = status }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.wrapContentWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { onStatusUpdate(selectedStatus) },
                        modifier = Modifier.wrapContentWidth(),
                        enabled = selectedStatus != kot.status.lowercase()
                    ) {
                        Text("Update Status")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusOptionCard(
    status: String,
    isSelected: Boolean,
    isCurrentStatus: Boolean,
    onClick: () -> Unit
) {
    val (backgroundColor, contentColor, borderColor) = when (status) {
        "pending" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.error
        )
        "preparing" -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            MaterialTheme.colorScheme.tertiary
        )
        "ready" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.primary
        )
        "served" -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            MaterialTheme.colorScheme.secondary
        )
        "cancelled" -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outline
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outline
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) backgroundColor else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(
            enabled = true,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Radio button indicator
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = contentColor,
                        unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                
                Column {
                    Text(
                        text = status.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = contentColor
                    )
                    if (isCurrentStatus) {
                        Text(
                            text = "Current",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Status indicator dot
            Surface(
                shape = RoundedCornerShape(50),
                color = contentColor,
                modifier = Modifier.size(12.dp)
            ) {}
        }
    }
}