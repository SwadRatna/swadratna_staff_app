package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class BillLineItem(
    val id: Int,
    val instructions: String?,
    @SerializedName("kot_id") val kotId: Int,
    @SerializedName("menu_item") val menuItem: BillMenuItem?,
    @SerializedName("menu_item_id") val menuItemId: Int,
    val price: Double,
    val quantity: Int,
    val status: String,
    @SerializedName("total_price") val totalPrice: Double
)
