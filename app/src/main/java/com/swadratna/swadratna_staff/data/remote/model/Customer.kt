package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName


// 1. Data class for the inner 'user' object
data class Customer(
    val id: Int,
    val tenant_id: Int,
    val created_at: String,
    val updated_at: String,
    val name: String,
    val email: String,
    // Note: It's often better to name variables using camelCase in Kotlin
    // If you use a JSON library like Gson, you'd use @SerializedName("mobile_number")
    val mobile_number: String,
    val status: String,
    val user_role: String,
    val permissions: List<String>
)

// 2. Data class for the top-level response
data class CustomerResponse(
    val created: Boolean,
    @SerializedName("user") val customer: Customer
)