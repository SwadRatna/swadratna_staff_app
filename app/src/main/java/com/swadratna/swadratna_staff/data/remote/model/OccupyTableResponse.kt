package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class OccupyTableResponse(
    val order: OrderDetails,
    @SerializedName("table_occupancy") val tableOccupancy: TableOccupancyDetails
)
