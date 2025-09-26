package com.swadratna.swadratna_staff.ui.screens.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.swadratna.swadratna_staff.navigation.NavigationRoute

data class Table(
    val tableNumber: Int, var personName: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(navController: NavController) {
    val tables = remember {
        mutableStateListOf(
            *(1..14).map {
                if (it == 4) {
                    Table(tableNumber = it, personName = "Vivek Kumar")
                } else {
                    Table(tableNumber = it)
                }
            }.toTypedArray()
        )
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedTable by remember { mutableStateOf<Table?>(null) }

    if (showDialog && selectedTable != null) {
        AssignTableDialog(
            tableNumber = selectedTable!!.tableNumber,
            onDismiss = { showDialog = false },
            onSubmit = { fullName, _ ->
                val index = tables.indexOfFirst { it.tableNumber == selectedTable!!.tableNumber }
                if (index != -1) {
                    tables[index] = tables[index].copy(personName = fullName)
                }
                showDialog = false
            })
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tables) { table ->
                TableCard(
                    table = table, onClick = {
                        if (table.personName == null) {
                            selectedTable = table
                            showDialog = true
                        } else {
                            navController.navigate(
                                "${NavigationRoute.OrderTaking.route}/${table.tableNumber}/${table.personName}"
                            )
                        }
                    })
            }
        }
    }
}

@Composable
fun TableCard(table: Table, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .height(100.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (table.personName != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (table.personName != null) {
                Text(
                    text = "${table.tableNumber} ${table.personName}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            } else {
                Text(
                    text = "${table.tableNumber}.",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            }
        }
    }
}
