package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class Item(
    val id: Int,
    val instructions: String,
    val menu_item: MenuItem,
    val menu_item_id: Int,
    val price: Double,
    val quantity: Int,
    val status: String,
    val total_price: Double
)