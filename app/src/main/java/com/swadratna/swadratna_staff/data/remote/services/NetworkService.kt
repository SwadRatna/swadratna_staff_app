package com.swadratna.swadratna_staff.data.remote.services

import com.swadratna.swadratna_staff.data.remote.model.Customer
import com.swadratna.swadratna_staff.data.remote.model.CustomerBill
import com.swadratna.swadratna_staff.data.remote.model.MenuItem
import com.swadratna.swadratna_staff.data.remote.model.Table
import com.swadratna.swadratna_staff.data.remote.model.TokenRefreshRequest
import com.swadratna.swadratna_staff.data.remote.model.TokenRefreshResponse
import retrofit2.Response
import retrofit2.http.*

// 1. POST /auth/login
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String)

// 5. POST /kot
data class KotItem(val menuItemId: String, val quantity: Int)
data class KotRequest(val tableId: String, val items: List<KotItem>)
data class KotResponse(val orderId: String)

// 7. PATCH /approveBill/:orderId
data class ApproveBillResponse(val message: String)

// 8. PATCH /menuItemAvailability/:location/:menu
data class AvailabilityRequest(val isAvailable: Boolean)
data class AvailabilityResponse(val message: String)

interface ApiService {

    @POST("user/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<TokenRefreshResponse>

    @POST("user/refresh-token")
    suspend fun refreshToken(@Body tokenRefreshRequest: TokenRefreshRequest): Response<TokenRefreshResponse>

    // Get a list of tables for a specific location
    @GET("tables/{locationId}")
    suspend fun getTablesByLocation(@Path("locationId") locationId: Int): Response<List<Table>>

    // Search for users by mobile number and/or username
    @GET("users")
    suspend fun findUser(@Query("mobile") mobile: String?, @Query("username") username: String?): Response<Customer>

    // Get the menu for a specific location
    @GET("menus/{locationId}")
    suspend fun getMenuByLocation(@Path("locationId") locationId: Int): Response<List<MenuItem>>

    // Create a new KOT (Kitchen Order Ticket)
    @POST("kot")
    suspend fun createKot(@Body request: KotRequest): Response<KotResponse>

    // Find and retrieve a specific bill
    @GET("findBill")
    suspend fun findBill(@Query("orderId") orderId: String): Response<CustomerBill>

    // Approve a bill
    @PATCH("approveBill/{orderId}")
    suspend fun approveBill(@Path("orderId") orderId: String): Response<ApproveBillResponse>

    // Update the availability of a menu item for a specific location
    @PATCH("menuItemAvailability/{locationId}/{menuId}")
    suspend fun updateMenuItemAvailability(
        @Path("locationId") locationId: String,
        @Path("menuId") menuId: String,
        @Body request: AvailabilityRequest
    ): Response<AvailabilityResponse>
}