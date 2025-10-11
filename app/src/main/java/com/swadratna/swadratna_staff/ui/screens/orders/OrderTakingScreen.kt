package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.theme.RedGrey20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTakingScreen(
    tableNumber: Int,
    orderId: String,
    onBack: () -> Unit,
    navController: NavController, // Added navController
    viewModel: OrderManagementViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.getMenuItems("")
    }

    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id // Use category.id
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Table $tableNumber",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Order ID: $orderId",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { navController.navigate(NavigationRoute.Bill.createRoute(orderId)) },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Menu,
                            contentDescription = "Bill",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Bill", fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        selectedTab = 0
                    },
                    colors = if (selectedTab == 0) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.buttonColors(containerColor = RedGrey20),
                    modifier = Modifier.weight(1f)
                ) {
                    Row (verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        Text("Menu")
                    }
                }

                Button(
                    onClick = {
                        selectedTab = 1
                    },
                    colors = if (selectedTab == 1) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary ) else ButtonDefaults.buttonColors(containerColor = RedGrey20),
                    modifier = Modifier.weight(1f)
                ) {
                    Row (verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccountBox, contentDescription = "Orders")
                        Text("Orders")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (selectedTab) {
            0 -> OrderMenuScreen(
                modifier = Modifier.padding(paddingValues),
                tableNumber = tableNumber,
                orderId = orderId,
                navController = navController
            )
            1 -> OrdersScreen(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                orderID = orderId,
                viewModel = viewModel
            )
        }
    }
}