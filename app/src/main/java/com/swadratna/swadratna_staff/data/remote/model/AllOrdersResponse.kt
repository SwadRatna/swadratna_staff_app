package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class AllOrdersResponse(
    val orders: List<OrderListItem>,
    val pagination: PaginationInfo
)
