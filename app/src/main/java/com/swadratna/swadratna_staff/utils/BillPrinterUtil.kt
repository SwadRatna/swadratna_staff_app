package com.swadratna.swadratna_staff.utils

import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
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
        private const val CHAR_WIDTH = 32 // Characters per line for 58mm paperval
        val colItem = 15
        val colQty = 6
        val colPrice = 7
        val colAmt = 5

        fun pad(text: String, length: Int): String {
            return if (text.length >= length) text.substring(0, length)
            else text + " ".repeat(length - text.length)
        }

        data class KotPrintingItem(
            val menu_name: String,
            val quantity: Int,
        )

        /**
         * Generates ESC/POS formatted KOT text.
         * Layout mirrors the sample image: header, customer/table, items with Sl.No & Qty, total.
         */
        fun generateKOT(
            kotItems: List<KotPrintingItem>,
            headerLeft: String? = null, // e.g., "Token: 1000071" or a KOT number
            tableLabel: String? = null, // e.g., "Table 1"
            customerName: String? = null,
            createdAt: Date = Date()
        ): String {
            val sb = StringBuilder()

            // Widths tuned for 58mm paper (~32 chars)
            val colSlNo = 6
            val colName = 20
            val colQtyKot = 6

            // Header
            sb.append("[C]<b>KOT</b>\n\n")

            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(createdAt)
            val left = headerLeft?.takeIf { it.isNotBlank() } ?: ""
            sb.append("[L]$left[R]$dateStr\n")

            customerName?.let { name ->
                sb.append("Customer : ${name}\n")
            }

            tableLabel?.let { tbl ->
                sb.append("Table No. : ${tbl}\n")
            }

            sb.append("${"-".repeat(32)}\n")

            // Column headers
            sb.append(
                pad("Sl.No", colSlNo) +
                pad("Item Name", colName) +
                pad("Qty.", colQtyKot) + "\n"
            )

            // Items
            kotItems.forEachIndexed { index, item ->
                val qtyStr = String.format("%3s", item.quantity.toString())
                val formatted = formatItemName24(item.menu_name, maxLine = colName)

                sb.append(
                    pad((index + 1).toString(), colSlNo) +
                    pad(formatted.line1, colName) +
                    pad(qtyStr, colQtyKot) + "\n"
                )

                formatted.line2?.let { line2 ->
                    sb.append(
                        pad("", colSlNo) +
                        pad(line2, colName) +
                        pad("", colQtyKot) + "\n"
                    )
                }
            }

            sb.append("${"-".repeat(32)}\n")

            val totalItems = kotItems.sumOf { it.quantity }
            sb.append("[R]Total Items : ${totalItems}")

            return sb.toString()
        }

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

            sb.append("[C]GSTIN: 29AADCR0331P1ZL\n")
            sb.append("[C]FSSAI Lic No. 11221334000882\n")
            sb.append("\n")

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

            sb.append("${"-".repeat(32)}\n")

            sb.append(pad("Items", colItem) +
                    pad("Qty", colQty) +
                    pad("Rate", colPrice) +
                    pad("Amt", colAmt) + "\n"
            )

            lineItems.forEach { item ->
                val qty = item.quantity.toString()
                val unitPrice = item.price
                val totalPrice = item.totalPrice

                val qtyStr = String.format("%3s", qty)
                val priceStr = String.format("%6.2f", unitPrice).replace(".00", "")
                val totalStr = String.format("%7.2f", totalPrice).replace(".00", "")

                val formatted = formatItemName24(item.menuItem.name)

                sb.append(
                    formatted.line1 +
                            " ".repeat(15 - formatted.line1.length.coerceAtMost(15)) +
                            qtyStr + "   " +
                            priceStr + "   " +
                            totalStr +
                            "\n"
                )

                if (formatted.line2 != null) {
                    sb.append(formatted.line2 + "\n")
                }

                item.instructions?.let { instructions ->
                    if (instructions.isNotBlank()) {
                        sb.append("  Note: ${truncateText(instructions, 28)}\n")
                    }
                }
            }

            sb.append("${"-".repeat(32)}\n")

            val totalQty = lineItems.sumOf { it.quantity }
            val subTotalStr = String.format("%.2f", bill.subTotal)
            sb.append("${" ".repeat(8)}Total Qty: ${totalQty}${" ".repeat(3)}Sub: ${subTotalStr}\n")
            sb.append("${" ".repeat(34)}Total\n")

            val sgstAmount = String.format("%.2f", bill.taxAmount / 2)
            val cgstAmount = String.format("%.2f", bill.taxAmount / 2)
            sb.append("${" ".repeat(20)}SGST 2.5%${" ".repeat(3)}${sgstAmount}\n")
            sb.append("${" ".repeat(20)}CGST 2.5%${" ".repeat(3)}${cgstAmount}\n")

            if (bill.serviceCharge > 0) {
                val serviceChargeStr = String.format("%.2f", bill.serviceCharge)
                sb.append("${" ".repeat(15)}Service Charge${" ".repeat(3)}${serviceChargeStr}\n")
            }

            if (bill.discountAmount > 0) {
                val roundOffStr = String.format("%.2f", -bill.discountAmount.toDouble())
                sb.append("${" ".repeat(18)}Round off${" ".repeat(3)}${roundOffStr}\n")
            }

            val grandTotalStr = String.format("%.2f", bill.totalAmount)
            sb.append("${" ".repeat(10)}Grand Total ₹ ${grandTotalStr}\n")
            sb.append("\n")

            sb.append("${" ".repeat(20)}Thank You,\n")
            sb.append("${" ".repeat(20)}Visit Again!\n")
            sb.append("\n\n")

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

        private const val PREF_NAME = "printer_prefs"
        private const val KEY_PRINTER_ADDRESS = "default_printer_address"
        private const val KEY_PRINTER_NAME = "default_printer_name"

        fun saveDefaultPrinter(context: Context, address: String, name: String) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_PRINTER_ADDRESS, address)
                .putString(KEY_PRINTER_NAME, name)
                .apply()
        }

        fun getDefaultPrinter(context: Context): Pair<String?, String?> {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val address = prefs.getString(KEY_PRINTER_ADDRESS, null)
            val name = prefs.getString(KEY_PRINTER_NAME, null)
            return Pair(address, name)
        }

        fun clearDefaultPrinter(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        }

        /**
         * Select a printer to be set as default without printing
         */
        fun selectDefaultPrinter(
            context: Context,
            onDone: (success: Boolean, msg: String) -> Unit
        ) {
            if (!hasBluetoothPermissions(context)) {
                onDone(false, "Bluetooth permissions required")
                return
            }

            val printers = getPairedPrinters()
            if (printers.isEmpty()) {
                onDone(false, "No paired printer found")
                return
            }

            val names = printers.map { "${it.name}  (${it.address})" }.toTypedArray()
            AlertDialog.Builder(context)
                .setTitle("Select Default Printer")
                .setItems(names) { _, which ->
                    val chosen = printers[which]
                    saveDefaultPrinter(context, chosen.address, chosen.name)
                    onDone(true, "Default printer set to ${chosen.name}")
                }
                .setNegativeButton("Cancel") { _, _ -> onDone(false, "Cancelled") }
                .show()
        }

        /**
         * Show a system AlertDialog with paired printers; print when user picks one.
         * If a default printer is set, it tries to print directly.
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

            // Check for default printer
            val (defaultAddress, defaultName) = getDefaultPrinter(context)
            val printers = getPairedPrinters()

            if (defaultAddress != null) {
                val defaultPrinter = printers.find { it.address == defaultAddress }
                if (defaultPrinter != null) {
                    try {
                        val connectedConnection = defaultPrinter.connection.connect()
                        val printer = EscPosPrinter(
                            connectedConnection,
                            203,
                            PAPER_WIDTH_MM.toFloat(),
                            CHAR_WIDTH
                        )
                        printer.printFormattedTextAndCut(billText)
                        printer.disconnectPrinter()
                        onDone(true, "Printed successfully using $defaultName")
                        return
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Fallback to chooser if default printer fails?
                        // User requested: "if that printer available just print using this only"
                        // But if it fails, maybe we should let them choose another one or just fail.
                        // Let's try to fall back to chooser so they aren't stuck.
                        // Or maybe just return failure as per "you should not give option to slect veerytime"
                        // However, practical UX suggests if the default fails, maybe they want to pick another.
                        // Let's show a toast that default failed and open chooser.
                        // For now, I will stick to the chooser fallback for robustness.
                    }
                }
            }

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
        fun hasBluetoothPermissions(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
            }
        }

        /**
         * Generates a PDF file for sharing, saving it to the app's cache directory.
         */
        fun generatePdfFile(
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
                val lines = plainText.split("\n")

                // Calculate required height
                // 80mm width approx 300 points.
                val pageWidth = 300
                val lineHeight = 20f
                val marginTop = 40f
                val marginBottom = 40f
                val marginLeft = 20f
                // Dynamic height based on content length + margins
                val contentHeight = (lines.size * lineHeight) + marginTop + marginBottom
                // Ensure minimum height
                val pageHeight = contentHeight.coerceAtLeast(400f).toInt()

                // Create PDF document with "Receipt" dimensions
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                val currentPage = pdfDocument.startPage(pageInfo)

                val canvas = currentPage.canvas
                // Fill white background
                canvas.drawColor(android.graphics.Color.WHITE)

                val paint = Paint().apply {
                    textSize = 12f // Monospace-like size
                    isAntiAlias = true
                    color = android.graphics.Color.BLACK
                    // typeface = Typeface.MONOSPACE // Optional: use monospace for alignment if needed
                }

                val titlePaint = Paint().apply {
                    textSize = 14f
                    isFakeBoldText = true
                    isAntiAlias = true
                    color = android.graphics.Color.BLACK
                    textAlign = Paint.Align.CENTER
                }

                // Draw content
                var yPosition = marginTop

                // Title centered
                canvas.drawText("SWAD RATNA - Restaurant Bill", (pageWidth / 2).toFloat(), yPosition, titlePaint)
                yPosition += lineHeight * 2

                // Draw bill content line by line
                lines.forEach { line ->
                    if (line.trim().isNotEmpty()) {
                        // Simple left alignment for now as our text generation relies on spaces
                        // Since we are using a narrower width, we might need to ensure the text fits.
                        // The plainText is generated with ~32 chars width for 58mm. 
                        // 300pts width with 12f text size should fit ~32-40 chars easily.
                        canvas.drawText(line, marginLeft, yPosition, paint)
                    }
                    yPosition += lineHeight
                }

                pdfDocument.finishPage(currentPage)

                // Save PDF to cache directory for sharing
                // Using externalCacheDir if available, else cacheDir
                val cachePath = File(context.cacheDir, "bills")
                cachePath.mkdirs()
                
                val fileName = "Bill_${billDetail.bill.billNumber}_${System.currentTimeMillis()}.pdf"
                val file = File(cachePath, fileName)

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
    }
}