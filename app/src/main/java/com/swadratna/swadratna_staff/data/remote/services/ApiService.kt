package com.swadratna.swadratna_staff.data.remote.services

import com.swadratna.swadratna_staff.RegisterDeviceTokenRequest
import com.swadratna.swadratna_staff.RegisterDeviceTokenResponse
import com.swadratna.swadratna_staff.data.remote.model.AllOrdersResponse
import com.swadratna.swadratna_staff.data.remote.model.CustomerBill
import com.swadratna.swadratna_staff.data.remote.model.CustomerResponse
import com.swadratna.swadratna_staff.data.remote.model.MenuResponse
import com.swadratna.swadratna_staff.data.remote.model.OccupyTableRequest
import com.swadratna.swadratna_staff.data.remote.model.OccupyTableResponse
import com.swadratna.swadratna_staff.data.remote.model.Staff_User
import com.swadratna.swadratna_staff.data.remote.model.TableListResponse

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
import com.swadratna.swadratna_staff.data.remote.model.KotListResponse
import com.swadratna.swadratna_staff.data.remote.model.KotStatusUpdateRequest
import com.swadratna.swadratna_staff.data.remote.model.KotStatusUpdateResponse

import com.swadratna.swadratna_staff.data.remote.model.OrderDetailsX
import com.swadratna.swadratna_staff.data.remote.model.BillDetail
import com.swadratna.swadratna_staff.data.remote.model.FreeTableRequest
import com.swadratna.swadratna_staff.data.remote.model.FreeTableResponse
import com.swadratna.swadratna_staff.data.remote.services.RecordPaymentRequest
import com.swadratna.swadratna_staff.data.remote.services.RecordPaymentResponse
import com.swadratna.swadratna_staff.data.remote.model.SalesResponse

data class BillActionRequest(
    val action: String,
)


interface ApiService {

    @POST("/api/v1/staff/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<Staff_User>

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

    @PATCH("/api/v1/staff/menu/availability/{locationId}/{menuId}")
    suspend fun updateMenuItemAvailability(
        @Path("locationId") locationId: String,
        @Path("menuId") menuId: String,
        @Body request: AvailabilityRequest
    ): Response<AvailabilityResponse>

    @GET("/api/v1/staff/orders/detail/{orderID}")
    suspend fun getOrderDetail(@Path("orderID") orderID: String): Response<OrderDetailsX>

    @GET("/api/v1/staff/bill")
    suspend fun getBillDetails(@Header("X-Key") xKey: String, @Query("orderId") orderId: String): Response<BillDetail>

    @GET("/api/v1/staff/orders/{locationId}")
    suspend fun getAllOrders(
        @Path("locationId") locationId: Int,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<AllOrdersResponse>

    @GET("/api/v1/staff/kots")
    suspend fun getKots(
        @Query("location_id") locationId: Int,
        @Query("pending_only") pendingOnly: Boolean? = null
    ): Response<KotListResponse>

    @PATCH("/api/v1/staff/kots/{kotId}/status")
    suspend fun updateKotStatus(
        @Path("kotId") kotId: Int,
        @Body request: KotStatusUpdateRequest
    ): Response<KotStatusUpdateResponse>


    @POST("/api/v1/staff/tables/{tableId}/free")
    suspend fun freeTheTable(
        @Path("tableId") tableId: Int,
        @Body request: FreeTableRequest
    ): Response<FreeTableResponse>

    @POST("/api/v1/notifications/register-device")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenRequest
    ): Response<RegisterDeviceTokenResponse>

    @POST("/api/v1/staff/orders/create")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<CreateOrderResponse>

    @PATCH("/api/v1/staff/bill/{billId}/payment")
    suspend fun recordBillPayment(
        @Path("billId") billId: Int,
        @Body request: RecordPaymentRequest
    ): Response<RecordPaymentResponse>

    @GET("/api/v1/admin/sales")
    suspend fun getSales(
        @Query("date") date: String? = null,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("location_ids") locationIds: String? = null,
        @Query("min_amount") minAmount: Double? = null,
        @Query("max_amount") maxAmount: Double? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20,
        @Query("order_type") orderType: String? = null
    ): Response<SalesResponse>

}
