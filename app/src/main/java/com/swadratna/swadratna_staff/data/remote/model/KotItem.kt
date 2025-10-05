package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class KotItem(
    val id: Int,
    val instructions: String?,
    @SerializedName("menu_item") val menuItem: MenuItem,
    @SerializedName("menu_item_id") val menuItemId: Int,
    val price: Double,
    val quantity: Int,
    val status: String,
    @SerializedName("total_price") val totalPrice: Double
)
