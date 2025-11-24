package com.swadratna.swadratna_staff.data.remote.model

data class OrderXX(
    val created_at: String,
    val discount: Double,
    val id: Int,
    val location_id: Int,
    val order_date: String,
    val order_status: String,
    val order_value: Double,
    val restaurant_id: Int,
    val table_id: Int,
    val total_value: Double,
    val updated_at: String,
    val user_id: Int
)