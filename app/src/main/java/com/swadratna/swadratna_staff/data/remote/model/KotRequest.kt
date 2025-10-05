package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class KotRequest(
    @SerializedName("line_items") val lineItems: List<LineItem>,
    @SerializedName("order_id") val orderId: Int
)
