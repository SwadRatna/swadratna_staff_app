package com.swadratna.swadratna_staff.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberBluetoothPermissionLauncher(
    onPermissionGranted: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val currentOnPermissionGranted by rememberUpdatedState(onPermissionGranted)
    var showRationaleDialog by remember { mutableStateOf(false) }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        if (allGranted) {
            currentOnPermissionGranted()
        } else {
            // If denied, show a dialog explaining why or guiding to settings
            // For simplicity, we can show a Toast or a custom Dialog.
            // User requested: "will ask the user to grant permission there also by showing the dialog"
            showRationaleDialog = true
        }
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Bluetooth Permission Required") },
            text = { Text("This app needs Bluetooth access to connect to the printer. Please grant the permission in Settings.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRationaleDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                Button(onClick = { showRationaleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    return {
        if (BillPrinterUtil.hasBluetoothPermissions(context)) {
            currentOnPermissionGranted()
        } else {
            launcher.launch(permissions)
        }
    }
}
