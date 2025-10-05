package com.swadratna.swadratna_staff.data.remote.model

data class KotItem(
    val created_at: String,
    val id: Int,
    val instructions: String,
    val kot_id: Int,
    val menu_item_id: Int,
    val price: Int,
    val quantity: Int,
    val status: String,
    val tenant_id: Int,
    val updated_at: String
)