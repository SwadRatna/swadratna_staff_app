package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class BillDetail(
    val bill: Bill,
    val exists: Boolean,
    @SerializedName("line_items") val lineItems: List<BillLineItem>,
)
