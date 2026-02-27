package com.swadratna.swadratna_staff.data.remote.model

data class OrderStatusUpdateRequest(
    val status: String,
    val reason: String
)
