package com.swadratna.swadratna_staff.data.remote.model

data class TableOccupancy(
    val isOccupied: Boolean,
    val customer: Customer,
    val startTime: Long,
    val endTime: Long,
    val order: CustomerBill
)
