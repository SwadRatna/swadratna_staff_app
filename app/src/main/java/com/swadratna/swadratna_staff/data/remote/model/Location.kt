package com.swadratna.swadratna_staff.data.remote.model

data class Location(
    val address: Address,
    val created_at: String,
    val id: Int,
    val location_mobile_number: String,
    val restaurant_id: Int,
    val status: String,
    val tenant_id: Int,
    val updated_at: String
)