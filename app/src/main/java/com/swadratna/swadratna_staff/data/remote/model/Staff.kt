package com.swadratna.swadratna_staff.data.remote.model

data class Staff(
    val address: String,
    val created_at: String,
    val email: String,
    val emergency_contact: EmergencyContact,
    val employee_id: String,
    val id: Int,
    val join_date: String,
    val mobile_number: String,
    val name: String,
    val permissions: Any,
    val phone: String,
    val role: String,
    val salary: Double,
    val status: String,
    val store_id: Int,
    val tenant_id: Int,
    val updated_at: String
)