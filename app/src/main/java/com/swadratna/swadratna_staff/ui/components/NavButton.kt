package com.swadratna.swadratna_staff.ui.components

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun NavButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    unselectedContainerColor: Color,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 8.dp // Define the specific corner radius here

    // Determine button colors based on selection state
    val buttonColors = if (isSelected) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = unselectedContainerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    // Apply conditional border (used for unselected state)
    val buttonModifier = if (!isSelected) {
        // Apply border and shape (must be applied to the button itself for the border to match)
        modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(cornerRadius))
    } else {
        modifier
    }

    Button(
        onClick = {onClick()
            Log.i("TAG", "NavButton: lol")},
        colors = buttonColors,
        shape = RoundedCornerShape(cornerRadius),
        modifier = buttonModifier.height(50.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label)
        }
    }
}