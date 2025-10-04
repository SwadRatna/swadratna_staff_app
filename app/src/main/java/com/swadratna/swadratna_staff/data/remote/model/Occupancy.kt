package com.swadratna.swadratna_staff.data.remote.model

data class Occupancy(
    val occupied_from: String,
    val order_id: Int,
    val user_id: Int,
    val user_name: String,
    val user_phone: String
)