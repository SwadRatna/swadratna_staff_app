package com.swadratna.swadratna_staff.utils.permissions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Example ViewModel demonstrating how to use PermissionManager in logic.
 */
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    fun performRestrictedAction() {
        if (permissionManager.canUpdateMenu()) {
            // Perform action
            println("Action allowed")
        } else {
            // Handle permission denied
            println("Action denied")
        }
    }
}

/**
 * Example Composable demonstrating how to use PermissionManager in UI.
 * 
 * Usage:
 * 1. Inject PermissionManager into your ViewModel (preferred) or use EntryPoint if needed.
 * 2. Observe the permissions flow if you need reactive updates (e.g., if permissions change while on screen).
 * 3. Or just call the methods directly if you are inside a click handler or logic block.
 */
@Composable
fun PermissionExampleScreen(
    viewModel: ExampleViewModel = hiltViewModel(),
    // In real app, you might inject PermissionManager directly or pass it via CompositionLocal
    permissionManager: PermissionManager 
) {
    // 1. Reactive approach: Observe the permissions state
    // This ensures the UI updates if the user permissions change in the background
    val permissions by permissionManager.currentUserPermissions.collectAsState()
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Permission Example")

        // Check using the observed state (Reactive)
        if (permissions.contains(Permissions.MENU_UPDATE)) {
            Button(onClick = { viewModel.performRestrictedAction() }) {
                Text("Update Menu (Visible if allowed)")
            }
        }
        
        // Or use the convenience methods on the manager (Non-reactive / Snapshot)
        // Note: Since PermissionManager holds a StateFlow, these methods return the *current* value.
        // If you want the UI to recompose when permissions change, you MUST collect the flow above.
        // However, for simple conditional rendering where you don't expect permissions to change 
        // instantly without screen refresh, you can sometimes use this, but collecting state is safer.
        
        if (permissionManager.canViewReports()) {
            Text("Sales Report Graph", color = Color.Green)
        } else {
            Text("You don't have permission to view reports", color = Color.Red)
        }
    }
}
