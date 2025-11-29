package com.swadratna.swadratna_staff.data.remote.model

data class ParcelOrder(
    val id: Int,
    val tenant_id: Int,
    val created_at: String,
    val updated_at: String,
    val user_id: Int,
    val table_id: Int,
    val location_id: Int,
    val restaurant_id: Int,
    val total_value: Double,
    val order_date: String,
    val order_value: Double,
    val discount: Double,
    val order_status: String,
    val order_type: String,
    val status: String,
    val total_amount: Double,
    val discount_amount: Double,
    val points_applied: Double,
    val final_amount: Double,
    val customer_name: String?,
    val customer_phone: String?,
    val customer_email: String?,
    val last_non_served_kot_time: String? = null
)
