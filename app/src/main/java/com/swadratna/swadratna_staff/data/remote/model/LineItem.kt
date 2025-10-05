package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class LineItem(
    @SerializedName("menu_item_id") val menuItemId: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("instructions") val instructions: String?
)
