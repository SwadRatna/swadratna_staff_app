package com.swadratna.swadratna_staff.utils

import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.exceptions.EscPosConnectionException
import com.swadratna.swadratna_staff.data.remote.model.Address
import com.swadratna.swadratna_staff.data.remote.model.BillDetail
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillPrinterUtil {

    companion object {
        private const val PAPER_WIDTH_MM = 58 // Standard thermal printer width
        private const val CHAR_WIDTH = 32 // Characters per line for 58mm paper

        /**
         * Generates ESC/POS formatted bill text with store and customer information
         */
        fun generateBillText(
            billDetail: BillDetail,
            storeAddress: Address? = null,
            storeName: String = "SWAD RATNA",
            storePhone: String? = null,
            customerName: String? = null,
            customerMobile: String? = null,
            cashierName: String? = null
        ): String {
            val bill = billDetail.bill
            val lineItems = billDetail.lineItems

            val sb = StringBuilder()

            // Store Header - Centered and Bold
            sb.append("[C]<b>${storeName}</b>\n")

            // Store Address - Centered
            storeAddress?.let { address ->
                if (address.plot_no.isNotBlank()) sb.append("[C]${address.plot_no}\n")
                if (address.street_1.isNotBlank()) sb.append("[C]${address.street_1}\n")
                if (address.street_2.isNotBlank()) sb.append("[C]${address.street_2}\n")
                if (address.locality.isNotBlank()) sb.append("[C]${address.locality}\n")
                if (address.city.isNotBlank() && address.pincode.isNotBlank()) {
                    sb.append("[C]${address.city} - ${address.pincode}\n")
                }
            }

            // Store Phone - Centered
            storePhone?.let { phone ->
                sb.append("[C]${phone}\n")
            }

            // Business Details - Centered
            sb.append("[C]GSTIN: 29AADCR0331P1ZL\n")
            sb.append("[C]FSSAI Lic No. 11221334000882\n")
            sb.append("\n")

            // Customer Information
            customerName?.let { name ->
                sb.append("Name: ${name}")
                customerMobile?.let { mobile ->
                    sb.append(" (M: ${mobile})")
                }
                sb.append("\n")
            }

            // Bill Information - Two columns
            sb.append("Date: ${formatDate(bill.createdAt)}${" ".repeat(10)}Dine In: ${bill.tableId}\n")
            sb.append("Time: ${formatTime(bill.createdAt)}\n")
            cashierName?.let { cashier ->
                sb.append("Cashier: ${cashier}${" ".repeat(8)}Bill No.: ${bill.billNumber}\n")
            } ?: run {
                sb.append("${" ".repeat(20)}Bill No.: ${bill.billNumber}\n")
            }
            sb.append("Token No.: ${bill.orderId}\n")
            sb.append("\n")

            // Separator line
            sb.append("${"-".repeat(32)}\n")

            // Items Header with proper spacing
            sb.append("Item${" ".repeat(16)}Qty. Price Amount\n")
            sb.append("${"-".repeat(32)}\n")

            // Line Items with better formatting
            lineItems.forEach { item ->
                val itemName = truncateText(item.menuItem.name, 20)
                val qty = item.quantity.toString()
                val unitPrice = item.price / item.quantity
                val totalPrice = item.totalPrice

                // Item name
                sb.append("${itemName}\n")

                // Quantity, unit price, and total with proper alignment
                val qtyStr = String.format("%3s", qty)
                val priceStr = String.format("%6.2f", unitPrice)
                val totalStr = String.format("%7.2f", totalPrice)
                
                sb.append("${" ".repeat(20)}${qtyStr} ${priceStr} ${totalStr}\n")

                // Add instructions if available
                item.instructions?.let { instructions ->
                    if (instructions.isNotBlank()) {
                        sb.append("  Note: ${truncateText(instructions, 28)}\n")
                    }
                }
            }

            // Separator line
            sb.append("${"-".repeat(32)}\n")

            // Totals section with proper alignment
            val totalQty = lineItems.sumOf { it.quantity }
            val subTotalStr = String.format("%.2f", bill.subTotal)
            sb.append("${" ".repeat(8)}Total Qty: ${totalQty}${" ".repeat(3)}Sub: ${subTotalStr}\n")
            sb.append("${" ".repeat(34)}Total\n")
            
            // Tax breakdown
            val sgstAmount = String.format("%.2f", bill.taxAmount / 2)
            val cgstAmount = String.format("%.2f", bill.taxAmount / 2)
            sb.append("${" ".repeat(20)}SGST 2.5%${" ".repeat(3)}${sgstAmount}\n")
            sb.append("${" ".repeat(20)}CGST 2.5%${" ".repeat(3)}${cgstAmount}\n")

            // Service charge if applicable
            if (bill.serviceCharge > 0) {
                val serviceChargeStr = String.format("%.2f", bill.serviceCharge)
                sb.append("${" ".repeat(15)}Service Charge${" ".repeat(3)}${serviceChargeStr}\n")
            }

            // Round off if applicable
            if (bill.discountAmount > 0) {
                val roundOffStr = String.format("%.2f", -bill.discountAmount.toDouble())
                sb.append("${" ".repeat(18)}Round off${" ".repeat(3)}${roundOffStr}\n")
            }

            // Grand Total - Prominent
            val grandTotalStr = String.format("%.2f", bill.totalAmount)
            sb.append("${" ".repeat(10)}Grand Total ₹ ${grandTotalStr}\n")
            sb.append("\n")

            // Footer - Centered
            sb.append("${" ".repeat(20)}Thank You,\n")
            sb.append("${" ".repeat(20)}Visit Again!\n")
            sb.append("\n\n\n")

            return sb.toString()
        }

        /**
         * Attempts to print via Bluetooth thermal printer
         */
        fun printViaBluetooth(context: Context, billText: String): Boolean {
            return try {
                val bluetoothConnection = BluetoothPrintersConnections.selectFirstPaired()
                if (bluetoothConnection != null) {
                    val printer = EscPosPrinter(
                        bluetoothConnection,
                        203,
                        PAPER_WIDTH_MM.toFloat(),
                        CHAR_WIDTH
                    )
                    printer.printFormattedText(billText)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        /**
         * Generates PDF for simulation when no printer is available
         */
        fun generatePdfBill(
            context: Context,
            billDetail: BillDetail,
            storeAddress: Address? = null,
            storeName: String = "SWAD RATNA",
            storePhone: String? = null,
            customerName: String? = null,
            customerMobile: String? = null,
            cashierName: String? = null
        ): File? {
            return try {
                val billText = generateBillText(
                    billDetail = billDetail,
                    storeAddress = storeAddress,
                    storeName = storeName,
                    storePhone = storePhone,
                    customerName = customerName,
                    customerMobile = customerMobile,
                    cashierName = cashierName
                )
                val plainText = convertEscPosToPlainText(billText)

                // Create PDF document
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                var currentPage = pdfDocument.startPage(pageInfo)

                var canvas = currentPage.canvas
                val paint = Paint().apply {
                    textSize = 12f
                    isAntiAlias = true
                }

                val titlePaint = Paint().apply {
                    textSize = 16f
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                // Draw content
                var yPosition = 50f
                val lineHeight = 20f
                val leftMargin = 50f

                // Title
                canvas.drawText("SWAD RATNA - Restaurant Bill", leftMargin, yPosition, titlePaint)
                yPosition += lineHeight * 2

                // Draw bill content line by line
                plainText.split("\n").forEach { line ->
                    if (line.trim().isNotEmpty()) {
                        canvas.drawText(line, leftMargin, yPosition, paint)
                    }
                    yPosition += lineHeight

                    // Start new page if needed
                    if (yPosition > 800) {
                        pdfDocument.finishPage(currentPage)
                        currentPage = pdfDocument.startPage(pageInfo)
                        canvas = currentPage.canvas
                        yPosition = 50f
                    }
                }

                pdfDocument.finishPage(currentPage)

                // Save PDF
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val fileName =
                    "Bill_${billDetail.bill.billNumber}_${System.currentTimeMillis()}.pdf"
                val file = File(downloadsDir, fileName)

                val fos = FileOutputStream(file)
                pdfDocument.writeTo(fos)
                pdfDocument.close()
                fos.close()

                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        /**
         * Converts ESC/POS formatted text to plain text for PDF
         */
        private fun convertEscPosToPlainText(escPosText: String): String {
            return escPosText
                .replace("[C]", "")
                .replace("[L]", "")
                .replace("[R]", "")
                .replace("<b>", "")
                .replace("</b>", "")
                .replace(Regex("\\[.*?\\]"), "")
        }

        /**
         * Truncates text to fit within specified length
         */
        private fun truncateText(text: String, maxLength: Int): String {
            return if (text.length <= maxLength) {
                text
            } else {
                text.substring(0, maxLength - 3) + "..."
            }
        }

        /**
         * Formats date string for display
         */
        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateString
            }
        }

        /**
         * Formats time string for display
         */
        private fun formatTime(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateString
            }
        }

        /* ------------------------------------------------------ */
        /*  Built-in Bluetooth-printer chooser                    */
        /* ------------------------------------------------------ */

        /**
         * Simple holder so we can show friendly names in the picker
         */
        data class PrinterDevice(
            val name: String,
            val address: String,
            val connection: com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
        )

        /**
         * Return list of already-paired printers
         */
        private fun getPairedPrinters(): List<PrinterDevice> {
            val printers = mutableListOf<PrinterDevice>()
            
            try {
                // Get all Bluetooth connections
                val bluetoothConnections = BluetoothPrintersConnections().getList()
                
                if (bluetoothConnections != null) {
                    for (conn in bluetoothConnections) {
                        val device = conn.getDevice()
                        val deviceName = device.name ?: "Unknown"
                        
                        // Include all paired devices, not just those with specific device classes
                        // This helps with "Inner printer" and other non-standard printers
                        printers.add(PrinterDevice(
                            name = deviceName,
                            address = device.address,
                            connection = conn
                        ))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return printers
        }

        /**
         * Show a system AlertDialog with paired printers; print when user picks one.
         * Usage:
         *   BillPrinterUtil.printWithChooser(context, billText) { ok, msg ->
         *       Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
         *   }
         */
        fun printWithChooser(
            context: Context,
            billText: String,
            onDone: (success: Boolean, msg: String) -> Unit
        ) {
            // Check Bluetooth permissions first
            if (!hasBluetoothPermissions(context)) {
                onDone(false, "Bluetooth permissions required. Please grant permissions in settings.")
                return
            }

            val printers = getPairedPrinters()
            if (printers.isEmpty()) {
                onDone(false, "No paired printer found. Please pair one in Android settings.")
                return
            }

            val names = printers.map { "${it.name}  (${it.address})" }.toTypedArray()
            AlertDialog.Builder(context)
                .setTitle("Select Printer")
                .setItems(names) { _, which ->
                    val chosen = printers[which]
                    try {
                        // First, attempt to connect the chosen connection
                        val connectedConnection = chosen.connection.connect()
                        
                        // Create printer with the connected connection
                        val printer = EscPosPrinter(
                            connectedConnection,
                            203,
                            PAPER_WIDTH_MM.toFloat(),
                            CHAR_WIDTH
                        )
                        
                        // Print and cut
                        printer.printFormattedTextAndCut(billText)
                        
                        // Disconnect when done
                        printer.disconnectPrinter()
                        
                        onDone(true, "Printed successfully")
                    } catch (e: EscPosConnectionException) {
                        e.printStackTrace()
                        onDone(false, "Connection failed: ${e.message}. Make sure printer is powered on and within range.")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onDone(false, "Print failed: ${e.message}")
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> onDone(false, "Cancelled") }
                .show()
        }

        /**
         * Check if the app has required Bluetooth permissions
         */
        private fun hasBluetoothPermissions(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}