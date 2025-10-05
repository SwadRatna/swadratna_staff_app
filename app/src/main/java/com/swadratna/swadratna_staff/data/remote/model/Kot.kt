package com.swadratna.swadratna_staff.data.remote.model

data class Kot(
    val created_at: String,
    val created_by_id: Int,
    val id: Int,
    val kot_number: String,
    val location_id: Int,
    val order_id: Int,
    val restaurant_id: Int,
    val status: String,
    val table_id: Int,
    val tenant_id: Int,
    val updated_at: String
)