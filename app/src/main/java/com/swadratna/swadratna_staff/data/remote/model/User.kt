package com.swadratna.swadratna_staff.data.remote.model

data class User(
    val created_at: String,
    val email: String,
    val id: Int,
    val last_login_at: String,
    val mobile_number: String,
    val name: String,
    val permissions: List<String>,
    val status: String,
    val tenant_id: Int,
    val updated_at: String,
    val user_role: String
)