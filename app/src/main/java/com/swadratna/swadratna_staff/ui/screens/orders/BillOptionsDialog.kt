package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun BillOptionsDialog(
    onDismiss: () -> Unit,
    onRegenerate: (Boolean?, Double?, Double?, Double?, Boolean?, String?) -> Unit
) {
    var removeGst by remember { mutableStateOf(false) }
    var serviceCharge by remember { mutableStateOf("") }
    var tip by remember { mutableStateOf("") }
    var additionalDiscount by remember { mutableStateOf("") }
    var applyCampaign by remember { mutableStateOf(true) }
    var promoCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Bill Options") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Remove GST
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Remove GST", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = removeGst, onCheckedChange = { removeGst = it })
                }

                // Apply Campaign
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Apply Campaign", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = applyCampaign, onCheckedChange = { applyCampaign = it })
                }

                Divider()

                // Service Charge
                OutlinedTextField(
                    value = serviceCharge,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) serviceCharge = it },
                    label = { Text("Service Charge") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // Tip
                OutlinedTextField(
                    value = tip,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) tip = it },
                    label = { Text("Tip") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // Additional Staff Discount
                OutlinedTextField(
                    value = additionalDiscount,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) additionalDiscount = it },
                    label = { Text("Addl. Staff Discount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // Promo Code
                OutlinedTextField(
                    value = promoCode,
                    onValueChange = { promoCode = it },
                    label = { Text("Promo Code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onRegenerate(
                        if (removeGst) true else null,
                        serviceCharge.toDoubleOrNull(),
                        tip.toDoubleOrNull(),
                        additionalDiscount.toDoubleOrNull(),
                        if (applyCampaign) true else null,
                        promoCode.takeIf { it.isNotBlank() }
                    )
                }
            ) {
                Text("Generate Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
