package com.swadratna.swadratna_staff.data.remote.model

data class Table(
    val capacity: Int,
    val id: Int,
    val is_occupied: Boolean,
    val location_id: Int,
    val occupancy: Occupancy,
    val qr_code_img_url: String,
    val status: String,
    val table_id: String,
    val last_non_served_kot_time: String? = null
)