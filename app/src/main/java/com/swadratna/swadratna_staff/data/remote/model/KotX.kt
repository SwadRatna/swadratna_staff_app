package com.swadratna.swadratna_staff.data.remote.model

data class KotX(
    val created_at: String,
    val created_by_id: Int,
    val id: Int,
    val items: List<Item>,
    val kot_number: String,
    val prep_end_time: String,
    val prep_start_time: String,
    val served_at: String,
    val status: String
)