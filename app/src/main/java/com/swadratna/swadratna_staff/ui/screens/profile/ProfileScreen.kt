package com.swadratna.swadratna_staff.ui.screens.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.R
import com.swadratna.swadratna_staff.data.local.entities.StaffUser
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.components.NavButton
import com.swadratna.swadratna_staff.ui.screens.login.LoginViewModel
import com.swadratna.swadratna_staff.ui.theme.RedGrey20

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.swadratna.swadratna_staff.utils.BillPrinterUtil
import com.swadratna.swadratna_staff.utils.permissions.Permissions
import com.swadratna.swadratna_staff.utils.rememberBluetoothPermissionLauncher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffProfileScreen(
    viewModel: ProfileViewmodel = hiltViewModel(),
    navController: NavController
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val staffUser by viewModel.staffUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoggedOut by viewModel.logoutSuccess.collectAsState()
    val permissions by viewModel.permissionManager.currentUserPermissions.collectAsState()


    LaunchedEffect(isLoggedOut) {
        if(isLoggedOut) {
            navController.navigate(NavigationRoute.Login.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            IconButton(onClick = { navController.popBackStack()} ) {
                Icon(Icons.Filled.Close, contentDescription = "Back")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        )
        {
            // Staff Profile Header
            staffUser?.let { user ->
                StaffProfileHeader(user)
            } ?: run {
                // Show loading or placeholder when user data is not available
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text("User profile not available")
                    }
                }
            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Work Information
//            ProfileSection(title = "Work Information") {
//                ProfileMenuItem(
//                    icon = ImageVector.vectorResource(R.drawable.ic_badge),
//                    title = "Employee Details",
//                    subtitle = "ID, Department, Position",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//                ProfileMenuItem(
//                    icon = ImageVector.vectorResource(R.drawable.ic_schedule),
//                    title = "Work Schedule",
//                    subtitle = "View your shifts & timings",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//                ProfileMenuItem(
//                    icon = ImageVector.vectorResource(R.drawable.ic_calendar_month),
//                    title = "Attendance",
//                    subtitle = "Check-in/out history",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//                ProfileMenuItem(
//                    icon = Icons.Default.DateRange,
//                    title = "Leave Management",
//                    subtitle = "Request & track leaves",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Restaurant & Location
//            ProfileSection(title = "Restaurant & Location") {
//                ProfileMenuItem(
//                    icon = ImageVector.vectorResource(R.drawable.ic_store),
//                    title = "Restaurant Details",
//                    subtitle = "Branch name & information",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//                ProfileMenuItem(
//                    icon = Icons.Default.LocationOn,
//                    title = "Branch Location",
//                    subtitle = "Address & contact details",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//                ProfileMenuItem(
//                    icon = Icons.Default.Person,
//                    title = "Team Directory",
//                    subtitle = "View staff & managers",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Account & Settings
//            ProfileSection(title = "Account & Settings") {
//                ProfileMenuItem(
//                    icon = Icons.Default.Person,
//                    title = "Personal Information",
//                    subtitle = "Update your profile details",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//                ProfileMenuItem(
//                    icon = Icons.Default.Lock,
//                    title = "Change Password",
//                    subtitle = "Update your password",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//                ProfileMenuItem(
//                    icon = Icons.Default.Notifications,
//                    title = "Notifications",
//                    subtitle = "Manage notification preferences",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Support
//            ProfileSection(title = "Help & Support") {
//                ProfileMenuItem(
//                    icon = ImageVector.vectorResource(R.drawable.ic_help),
//                    title = "Help Center",
//                    subtitle = "FAQs & troubleshooting",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//                ProfileMenuItem(
//                    icon = Icons.Default.Phone,
//                    title = "Contact Manager",
//                    subtitle = "Get in touch with your manager",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//                ProfileMenuItem(
//                    icon = Icons.Default.Info,
//                    title = "About App",
//                    subtitle = "Version & app information",
//                    onClick = { /* TODO: Screen not implemented yet */ }
//                )
//            }

            // Printer Settings
            var defaultPrinterName by remember { mutableStateOf("None Selected") }
            val context = LocalContext.current

            // Load initial state
            LaunchedEffect(Unit) {
                val (_, name) = BillPrinterUtil.getDefaultPrinter(context)
                defaultPrinterName = name ?: "None Selected"
            }

            ProfileSection(title = "Management") {
                ProfileMenuItem(
                    icon = ImageVector.vectorResource(R.drawable.ic_inventory),
                    title = "Inventory",
                    subtitle = "Manage inventory items",
                    onClick = { navController.navigate(NavigationRoute.Inventory.route) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSection(title = "Table Management") {
                ProfileMenuItem(
                    icon = ImageVector.vectorResource(R.drawable.ic_table),
                    title = "Table QR Manager",
                    subtitle = "Generate and print table QR codes",
                    onClick = { navController.navigate(NavigationRoute.TableManager.route) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val performSelectPrinter = rememberBluetoothPermissionLauncher {
                BillPrinterUtil.selectDefaultPrinter(context) { success, msg ->
                    if (success) {
                        val (_, name) = BillPrinterUtil.getDefaultPrinter(context)
                        defaultPrinterName = name ?: "None Selected"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }

            ProfileSection(title = "Printer Settings") {
                ProfileMenuItem(
                    icon = ImageVector.vectorResource(R.drawable.ic_recipt),
                    title = "Default Printer",
                    subtitle = defaultPrinterName,
                    onClick = {
                        performSelectPrinter()
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Logout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }



    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    "Logout",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    "Are you sure you want to logout?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logOut()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StaffProfileHeader(staffUser: StaffUser) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar - Using initials from username
            val initials = staffUser.username.take(2).uppercase()
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Staff Name
            Text(
                text = staffUser.username,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Staff Email
            Text(
                text = staffUser.email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Role and Branch Info
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_badge),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = staffUser.role.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = staffUser.location.address.city,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Employee ID
            Text(
                text = "Employee ID: ${staffUser.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
            
            // Phone number
            if (staffUser.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Phone: ${staffUser.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
            
            // Location ID
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Location ID: ${staffUser.location.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}