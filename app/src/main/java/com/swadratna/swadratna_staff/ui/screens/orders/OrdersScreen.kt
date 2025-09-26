package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Order(
    val orderNumber: String,
    val customerName: String,
    val items: List<String>,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen() {
    val orders = listOf(
        Order("#1001", "Allan Johnson", listOf("Spaghetti bolognese", "Water"), "New"),
        Order("#1002", "Bob Williams", listOf("Chicken Burger", "Coca-cola"), "New"),
        Order("#1003", "Charlie Brown", listOf("Veggie Burger", "French Fries", "Water"), "In Progress"),
        Order("#1004", "Diana Prince", listOf("Pizza Margherita", "Sprite"), "In Progress"),
        Order("#1005", "Harry Eagle", listOf("Tuna Pizza", "Fanta Orange", "Water"), "Completed")
    )


    Column(modifier = Modifier) {
        SearchBar()
        FilterButtons()
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(orders) { order ->
                OrderCard(order = order)
            }
        }
    }
}

@Composable
fun SearchBar() {
    val searchQuery = remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            placeholder = { Text("Search orders...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun FilterButtons() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = { /* Handle filter */ }) {
            Text("New")
        }
        Button(onClick = { /* Handle filter */ }) {
            Text("In Progress")
        }
        Button(onClick = { /* Handle filter */ }) {
            Text("Scheduled")
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order ${order.orderNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = order.status,
                    color = when (order.status) {
                        "New" -> MaterialTheme.colorScheme.error
                        "In Progress" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = order.customerName,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            order.items.forEach { item ->
                Text(text = item)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrdersScreenPreview() {
    OrdersScreen()
}