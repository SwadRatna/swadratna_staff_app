package com.swadratna.swadratna_staff.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class NotificationSheetData(
    val title: String,
    val body: String,
    val type: String,
    val orderId: String?,
    val billId: String?,
    val tableNumber: String?,
    val deepLink: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
    data: NotificationSheetData,
    onDismiss: () -> Unit,
    onViewClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.body,
                style = MaterialTheme.typography.bodyLarge
            )
            
            if (!data.tableNumber.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Table: ${data.tableNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onViewClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Details")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Preview
@Composable
fun NotificationBottomSheetPreview() {
    val data = NotificationSheetData(
        title = "Bill Approval Request",
        body = "Bill approval requested for Order #2000073",
        type = "approve_bill",
        orderId = "2000073",
        billId = "2000059",
        tableNumber = "2000020",
        deepLink = "deeplink=swadratna://bill-approval?billId=2000059&orderId=2000073"
    )

    NotificationBottomSheet(data = data, onDismiss = { }, onViewClick= {})

}
