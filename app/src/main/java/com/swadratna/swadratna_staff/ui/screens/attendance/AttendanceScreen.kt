package com.swadratna.swadratna_staff.ui.screens.attendance

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.data.remote.model.AttendanceModifyRequest
import com.swadratna.swadratna_staff.data.remote.model.Staff
import com.swadratna.swadratna_staff.ui.components.SearchBar
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    navController: NavController,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val filteredStaffList by viewModel.filteredStaffList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    var staffToModify by remember { mutableStateOf<Staff?>(null) }

    val view = LocalView.current
    val window = (view.context as Activity).window
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    DisposableEffect(Unit) {
        val insetsController = WindowCompat.getInsetsController(window, view)
        val originalStatusBarColor = window.statusBarColor
        val originalLightStatusBars = insetsController.isAppearanceLightStatusBars

        window.statusBarColor = primaryColor
        insetsController.isAppearanceLightStatusBars = false

        onDispose {
            window.statusBarColor = originalStatusBarColor
            insetsController.isAppearanceLightStatusBars = originalLightStatusBars
        }
    }

    LaunchedEffect(true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AttendanceUiEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Attendance") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is AttendanceState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AttendanceState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.fetchStaffMembers() }) {
                            Text("Retry")
                        }
                    }
                }
                is AttendanceState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchBar(
                            hintText = "Search Staff...",
                            query = searchQuery,
                            onQueryChanged = viewModel::onSearchQueryChanged,
                            modifier = Modifier.padding(16.dp)
                        )

                        if (filteredStaffList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No staff found for \"$searchQuery\"" else "No staff members found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredStaffList) { staff ->
                                    StaffItem(
                                        staff = staff,
                                        onCheckIn = { viewModel.checkIn(staff.id) },
                                        onCheckOut = { viewModel.checkOut(staff.id) },
                                        onModify = { staffToModify = staff }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    staffToModify?.let { staff ->
        ModifyAttendanceDialog(
            staff = staff,
            onDismiss = { staffToModify = null },
            onSubmit = { request ->
                viewModel.modifyAttendance(staff.id, request)
                staffToModify = null
            }
        )
    }
}

@Composable
fun StaffItem(
    staff: Staff,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onModify: () -> Unit
) {
    // Use top-level fields from Staff model as primary source of truth, fallback to nested object if needed
    val checkInTime = staff.today_check_in ?: staff.current_attendance?.check_in_time
    val checkOutTime = staff.today_check_out ?: staff.current_attendance?.check_out_time
    
    val hasCheckInTime = !checkInTime.isNullOrBlank()
    val hasCheckOutTime = !checkOutTime.isNullOrBlank()
    
    // Status Logic
    val isCheckedIn = hasCheckInTime && !hasCheckOutTime
    val isCheckedOut = hasCheckInTime && hasCheckOutTime

    // Button States Logic:
    // 1. If NO check-in time, show "Check In" enabled.
    // 2. If check-in time EXISTS, disable "Check In" (prevent "already checked in" error).
    // 3. If check-in time EXISTS but check-out time is MISSING, show "Check Out" enabled.
    // 4. If BOTH exist, disable "Check Out" (already done).
    
    val canCheckIn = !hasCheckInTime 
    val canCheckOut = hasCheckInTime && !hasCheckOutTime

    // Can modify if any attendance record exists
    val canModify = hasCheckInTime

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = staff.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = staff.role.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (staff.mobile_number.isNotEmpty()) {
                    Text(
                        text = staff.mobile_number,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Show Times if available
                if (hasCheckInTime) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "In: ${checkInTime}${if (hasCheckOutTime) " | Out: ${checkOutTime}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onCheckIn,
                        modifier = Modifier.weight(1f),
                        enabled = canCheckIn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        Text("Check In")
                    }
                    Button(
                        onClick = onCheckOut,
                        modifier = Modifier.weight(1f),
                        enabled = canCheckOut,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        Text("Check Out")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onModify,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canModify,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Modify Attendance")
                }
            }

            // Status Indicator
            val (statusText, statusColor) = when {
                isCheckedOut -> "Checked Out" to MaterialTheme.colorScheme.secondary
                isCheckedIn -> "Checked In" to MaterialTheme.colorScheme.primary
                else -> null to null
            }

            if (statusText != null && statusColor != null) {
                Surface(
                    color = if (statusText == "Checked In") Color.Green.copy(alpha = 0.2F) else statusColor.copy(alpha = 0.2F),
                    contentColor = statusColor,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 8.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyAttendanceDialog(
    staff: Staff,
    onDismiss: () -> Unit,
    onSubmit: (AttendanceModifyRequest) -> Unit
) {
    // Initialize with existing data
    val initialCheckIn = staff.today_check_in ?: staff.current_attendance?.check_in_time ?: ""
    val initialCheckOut = staff.today_check_out ?: staff.current_attendance?.check_out_time ?: ""
    val initialStatus = staff.today_status ?: staff.current_attendance?.status ?: ""

    var checkInTime by remember { mutableStateOf(initialCheckIn) }
    var checkOutTime by remember { mutableStateOf(initialCheckOut) }
    var status by remember { mutableStateOf(initialStatus) }
    var notes by remember { mutableStateOf("") }
    var isLate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modify Attendance: ${staff.name}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = checkInTime,
                    onValueChange = { checkInTime = it },
                    label = { Text("Check In Time (e.g. 9:00 AM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = checkOutTime,
                    onValueChange = { checkOutTime = it },
                    label = { Text("Check Out Time (e.g. 6:00 PM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                var statusExpanded by remember { mutableStateOf(false) }
                val statusOptions = listOf("present", "absent")

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    status = option
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isLate,
                        onCheckedChange = { isLate = it }
                    )
                    Text("Is Late")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val request = AttendanceModifyRequest(
                        check_in_time = checkInTime.takeIf { it.isNotBlank() },
                        check_out_time = checkOutTime.takeIf { it.isNotBlank() },
                        status = status.takeIf { it.isNotBlank() },
                        notes = notes.takeIf { it.isNotBlank() },
                        is_late = isLate
                    )
                    onSubmit(request)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
