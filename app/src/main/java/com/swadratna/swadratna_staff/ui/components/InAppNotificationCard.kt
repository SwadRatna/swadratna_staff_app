package com.swadratna.swadratna_staff.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.swadratna.swadratna_staff.R

@Composable
fun InAppNotificationCard(
    title: String,
    message: String,
    type: String,
    orderId: Int? = null,
    tableNumber: Int? = null,
    onViewOrderClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(true) }

    // Auto-dismiss after 5 seconds
    LaunchedEffect(Unit) {
        delay(5000)
        visible = false
        delay(300) // Wait for animation to complete
        onDismiss()
    }
    
    // Handle manual dismiss
    LaunchedEffect(visible) {
        if (!visible) {
            delay(300) // Wait for animation to complete
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        Card(
            // Use Material 3 elevation and colors
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface, // Use surface color for better theming
            )
        ) {

            // --- Card Content ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top // Align content to the top
            ) {

                // 1. Dynamic Icon (New Addition)
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_recipt),
                    contentDescription = "$type notification",
                    tint = MaterialTheme.colorScheme.primary, // Use dynamic color
                    modifier = Modifier.padding(top = 4.dp).size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 2. Text Content (Title and Message)
                Column(
                    modifier = Modifier.weight(1f) // Takes up most of the space
                ) {

                    // Title
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Message
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Action Button (View Order)
                    if (onViewOrderClick != {}) { // Only show button if click handler is provided
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = {
                                visible = false
                                onViewOrderClick()
                            },
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "VIEW DETAILS", // Changed text for general use
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 3. Dismiss Button
                IconButton(
                    onClick = { 
                        visible = false 
                    },
                    modifier = Modifier.align(Alignment.Top).size(32.dp) // Smaller touch target for dismiss
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss notification",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// Data class to hold notification information (Kept for context)
data class InAppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val orderId: Int? = null,
    val tableNumber: Int? = null,
    val deepLink: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Preview(showBackground = true, name = "New Order Notification")
@Composable
fun InAppNotificationCardPreview() {
    // A simple test theme with a primary color
    val testColorScheme = lightColorScheme(
        primary = Color(0xFF00796B), // Teal
        surface = Color.White,
        onSurface = Color.Black,
        onSurfaceVariant = Color.DarkGray
    )
    MaterialTheme(colorScheme = testColorScheme) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5))
        ) {
            InAppNotificationCard(
                title = "New Table Order",
                message = "Table 5 has submitted order #202. Tap to view details.",
                type = "new_order",
                onViewOrderClick = { /* Clicked */ },
                onDismiss = { /* Dismissed */ }
            )
        }
    }
}