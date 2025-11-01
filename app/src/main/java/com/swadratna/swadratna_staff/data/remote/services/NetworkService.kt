package com.swadratna.swadratna_staff.data.remote.services

import com.swadratna.swadratna_staff.data.remote.model.Customer
import com.swadratna.swadratna_staff.data.remote.model.CustomerBill
import retrofit2.Response
import retrofit2.http.*

// 1. POST /auth/login
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)

// 5. POST /kot
data class KotItem(val menuItemId: String, val quantity: Int)

// 7. PATCH /approveBill/:orderId
data class ApproveBillResponse(val message: String)

// 8. PATCH /menuItemAvailability/:location/:menu
data class AvailabilityRequest(val isAvailable: Boolean)
data class AvailabilityResponse(val message: String)
