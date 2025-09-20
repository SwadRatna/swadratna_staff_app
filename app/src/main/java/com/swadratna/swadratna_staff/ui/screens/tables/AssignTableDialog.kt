package com.swadratna.swadratna_staff.ui.screens.tables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AssignTableDialog(
    tableNumber: Int,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Assign Table $tableNumber", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("User's Full Name") },
                    placeholder = { Text("e.g. Alice Smith") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text("Contact Information") },
                    placeholder = { Text("e.g. +1 (555) 123-4567 or alice@example.com") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = "Table $tableNumber",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Table Assignment") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (fullName.isNotBlank() && contactInfo.isNotBlank()) {
                            onSubmit(fullName, contactInfo)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Submit User", color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp)
                }
            }
        }
    }
}