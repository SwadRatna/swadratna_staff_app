package com.swadratna.swadratna_staff.data.remote.model

data class TableListResponse(
    val tables: List<Table>,
    val parcel_orders: List<ParcelOrder>
)
