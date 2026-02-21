package com.swadratna.swadratna_staff.utils

import java.text.DecimalFormat
import java.util.Locale

object CurrencyUtils {
    fun formatPrice(amount: Double): String {
        return "₹%.2f".format(Locale.US, amount)
    }

    fun formatPriceNoSymbol(amount: Double): String {
        return "%.2f".format(Locale.US, amount)
    }
}
