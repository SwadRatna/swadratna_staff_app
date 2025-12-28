package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class CancelKotItemsRequest(
    @SerializedName("item_ids")
    val itemIds: List<Int>
)
