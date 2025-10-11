package com.swadratna.swadratna_staff.data.remote.services

import com.swadratna.swadratna_staff.data.remote.model.CustomerBill
import com.swadratna.swadratna_staff.data.remote.model.CustomerResponse
import com.swadratna.swadratna_staff.data.remote.model.MenuResponse
import com.swadratna.swadratna_staff.data.remote.model.OccupyTableRequest
import com.swadratna.swadratna_staff.data.remote.model.OccupyTableResponse
import com.swadratna.swadratna_staff.data.remote.model.Staff_User
import com.swadratna.swadratna_staff.data.remote.model.TableListResponse
import com.swadratna.swadratna_staff.data.remote.model.TokenRefreshRequest
import com.swadratna.swadratna_staff.data.remote.model.TokenRefreshResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Header
import com.swadratna.swadratna_staff.data.remote.model.KotRequest
import com.swadratna.swadratna_staff.data.remote.model.KotResponse

import com.swadratna.swadratna_staff.data.remote.model.OrderDetailsX
import com.swadratna.swadratna_staff.data.remote.model.BillDetail

data class BillActionRequest(
    val action: String,
    val reason: String
)


interface ApiService {

    @POST("/api/v1/staff/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<Staff_User>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): Response<TokenRefreshResponse>

    @GET("/api/v1/staff/tables/{locationId}")
    suspend fun getTablesByLocation(@Path("locationId") locationId: Int): Response<TableListResponse>

    @GET("/api/v1/staff/users")
    suspend fun getOrCreateCustomer(
        @Query("mobile") mobile: String?,
        @Query("userName") username: String?
    ): Response<CustomerResponse>

    @POST("/api/v1/staff/occupyTable")
    suspend fun occupyTable(@Body request: OccupyTableRequest): Response<OccupyTableResponse>

    @GET("/api/v1/staff/menu/{locationId}")
    suspend fun getMenu(
        @Path("locationId") locationId: Int,
        @Query("search") searchQuery: String? = null
    ): Response<MenuResponse>
    @POST("/api/v1/staff/kot")
    suspend fun createKot(@Body request: KotRequest): Response<KotResponse>

    @GET("findBill")
    suspend fun findBill(@Query("orderId") orderId: String): Response<CustomerBill>


    @PATCH("api/v1/staff/approveBill/{billId}")
    suspend fun updateBillStatus(
        @Path("billId") billId: Int,
        @Body request: BillActionRequest
    ): Response<ApproveBillResponse>

    @PATCH("menuItemAvailability/{locationId}/{menuId}")
    suspend fun updateMenuItemAvailability(
        @Path("locationId") locationId: String,
        @Path("menuId") menuId: String,
        @Body request: AvailabilityRequest
    ): Response<AvailabilityResponse>

    @GET("/api/v1/staff/orders/detail/{orderID}")
    suspend fun getOrderDetail(@Path("orderID") orderID: String): Response<OrderDetailsX>

    @GET("/api/v1/staff/bill")
    suspend fun getBillDetails(@Header("X-Key") xKey: String, @Query("orderId") orderId: String): Response<BillDetail>
}
