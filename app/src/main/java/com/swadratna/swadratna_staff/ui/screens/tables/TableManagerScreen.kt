package com.swadratna.swadratna_staff.ui.screens.tables

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.swadratna.swadratna_staff.data.remote.model.Table
import com.swadratna.swadratna_staff.ui.screens.orders.OrderManagementViewModel
import com.swadratna.swadratna_staff.ui.screens.orders.TableListState
import com.swadratna.swadratna_staff.utils.BillPrinterUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import org.json.JSONObject
import com.swadratna.swadratna_staff.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableManagerScreen(
    navController: NavController,
    viewModel: OrderManagementViewModel = hiltViewModel()
) {
    val tableListState by viewModel.tableListState.collectAsState()
    val staffLocationId by viewModel.staffLocationId.collectAsState()
    var selectedTable by remember { mutableStateOf<Table?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }

    LaunchedEffect(staffLocationId) {
        staffLocationId?.let {
            viewModel.getTables(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Table Manager") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (tableListState) {
                is TableListState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is TableListState.Error -> {
                    Text(
                        text = (tableListState as TableListState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is TableListState.Success -> {
                    val tables = (tableListState as TableListState.Success).tables.tables
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tables) { table ->
                            TableManagerItem(table = table) {
                                selectedTable = table
                                showQrDialog = true
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQrDialog && selectedTable != null) {
        QRCodeDialog(
            table = selectedTable!!,
            onDismiss = { showQrDialog = false }
        )
    }
}

@Composable
fun TableManagerItem(
    table: Table,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (table.is_occupied) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = table.table_id,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (table.is_occupied) "Occupied" else "Free",
                style = MaterialTheme.typography.bodyMedium,
                color = if (table.is_occupied) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QRCodeDialog(
    table: Table,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val qrContent = remember(table) {
        val json = JSONObject()
        json.put("location_id", table.location_id)
        json.put("table_id", table.id)
        json.toString()
    }
    
    val qrBitmap = remember(qrContent) {
        generateQRCode(qrContent)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Table QR Code",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = table.table_id, style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                } else {
                    Text("Failed to generate QR Code")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        qrBitmap?.let {
                            saveImageToGallery(context, it, "QR_${table.table_id}")
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                    
                    // Placeholder for Print - implementing proper thermal print needs more context on the printer setup
                    // But I'll add the button as requested.
                    Button(onClick = {
                        Toast.makeText(context, "Printing not implemented for QR yet", Toast.LENGTH_SHORT).show()
                        // TODO: Implement thermal printing for bitmap if needed
                    }) {
                        Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_recipt)
                            , contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Print")
                    }
                }
            }
        }
    }
}

fun generateQRCode(content: String, size: Int = 512): Bitmap? {
    return try {
        val hints = hashMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.MARGIN] = 1
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun saveImageToGallery(context: Context, bitmap: Bitmap, title: String) {
    val filename = "$title.jpg"
    var fos: OutputStream? = null
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SwadRatnaQR")
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = java.io.File(imagesDir, filename)
            fos = java.io.FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
