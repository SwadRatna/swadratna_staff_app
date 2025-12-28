package com.swadratna.swadratna_staff.data.remote.repositories

import com.swadratna.swadratna_staff.data.remote.model.AllOrdersResponse
import com.swadratna.swadratna_staff.data.remote.model.Customer
import com.swadratna.swadratna_staff.data.remote.model.CustomerBill
import com.swadratna.swadratna_staff.data.remote.model.KotRequest
import com.swadratna.swadratna_staff.data.remote.model.KotResponse

import com.swadratna.swadratna_staff.data.remote.model.MenuResponse
import com.swadratna.swadratna_staff.data.remote.model.OccupyTableRequest
import com.swadratna.swadratna_staff.data.remote.model.OccupyTableResponse
import com.swadratna.swadratna_staff.data.remote.model.OrderDetailsX
import com.swadratna.swadratna_staff.data.remote.model.TableListResponse
import com.swadratna.swadratna_staff.data.remote.model.BillDetail
import com.swadratna.swadratna_staff.data.remote.model.FreeTableRequest
import com.swadratna.swadratna_staff.data.remote.model.FreeTableResponse
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import com.swadratna.swadratna_staff.data.remote.services.ApproveBillResponse
import com.swadratna.swadratna_staff.data.remote.services.BillActionRequest
import com.swadratna.swadratna_staff.data.remote.services.RecordPaymentRequest
import com.swadratna.swadratna_staff.data.remote.services.RecordPaymentResponse
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import com.swadratna.swadratna_staff.utils.network.NetworkMonitor

class OrderManagementRepository @Inject constructor(
    @Named("authenticated")  private val apiService: ApiService,
    private val networkMonitor: NetworkMonitor
) {

    suspend fun getTablesByLocation(locationId: Int): Result<TableListResponse> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.getTablesByLocation(locationId)
            if (response.isSuccessful) {
                response.body()?.let { tableListResponse ->
                    Result.success(tableListResponse)
                } ?: Result.failure<TableListResponse>(Exception("No tables found"))
            } else {
                Result.failure<TableListResponse>(Exception("Failed to fetch tables: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } as Result<TableListResponse>
    }

    suspend fun getMenu(
        locationId: Int,
        searchQuery: String? = null
    ): Result<MenuResponse> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.getMenu(locationId, searchQuery)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Successful response, but returned an empty menu list."))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = "Failed to fetch menu. Code: ${response.code()}. Error: $errorBody"
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error while fetching menu. Please check connection.", e))
        } catch (e: Exception) {
            Result.failure(Exception("An unexpected error occurred during menu fetching.", e))
        }
    }

    suspend fun getOrCreateCustomer(mobile: String?, userName: String?): Result<Customer> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.getOrCreateCustomer(mobile, userName)
            if (response.isSuccessful) {
                response.body()?.let { customerResponse ->
                    Result.success(customerResponse.customer)
                } ?: Result.failure(Exception("Customer not found or created"))
            } else {
                Result.failure(Exception("Failed to get or create customer: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun occupyTable(request: OccupyTableRequest): Result<OccupyTableResponse> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.occupyTable(request)
            if (response.isSuccessful) {
                response.body()?.let { occupyTableResponse ->
                    Result.success(occupyTableResponse)
                } ?: Result.failure(Exception("Failed to occupy table: Empty response"))
            } else {
                Result.failure(Exception("Failed to occupy table: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createKot(kotRequest: KotRequest): Result<KotResponse> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.createKot(kotRequest)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Failed to create KOT: Empty response"))
            } else {
                Result.failure(Exception("Failed to create KOT: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findBill(orderId: String): Result<CustomerBill> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.findBill(orderId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Bill not found"))
            } else {
                Result.failure(Exception("Failed to find bill: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveBill(approvalAction: String , reason: String, orderId: Int): Result<ApproveBillResponse> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val request = BillActionRequest(action = approvalAction )
            val response = apiService.updateBillStatus(orderId, request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Failed to approve bill: Empty response"))
            } else {
                Result.failure(Exception("Failed to approve bill: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderDetail(orderID: String): Result<OrderDetailsX> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.getOrderDetail( orderID)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Order details not found"))
            } else {
                Result.failure(Exception("Failed to fetch order details: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBillDetails(
        orderId: String,
        removeGst: Boolean? = null,
        serviceCharge: Double? = null,
        tip: Double? = null,
        additionalStaffDiscount: Double? = null,
        applyCampaign: Boolean? = null,
        promoCode: String? = null
    ): Result<BillDetail> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.getBillDetails(
                "RMWvXbJYiKDtjtCEj03iGP",
                orderId,
                removeGst,
                serviceCharge,
                tip,
                additionalStaffDiscount,
                applyCampaign,
                promoCode
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Bill details not found"))
            } else {
                Result.failure(Exception("Failed to fetch bill details: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBillDetailsById(billId: String): Result<BillDetail> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.getBillDetailsById(billId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Bill details not found"))
            } else {
                Result.failure(Exception("Failed to fetch bill details: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllOrders(locationId: Int, page: Int, limit: Int): Result<AllOrdersResponse> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.getAllOrders(locationId, page, limit)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("No orders found"))
            } else {
                Result.failure(Exception("Failed to fetch all orders: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun freeTheTable(tableId: Int, cancelOrder: Boolean, reason: String): Result<FreeTableResponse> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val request = FreeTableRequest(cancel_order = cancelOrder, reason = reason)
            val response = apiService.freeTheTable(tableId, request)
            if (response.isSuccessful) {
                response.body()?.let { freeTableResponse ->
                    Result.success(freeTableResponse)
                } ?: Result.failure(Exception("Failed to free table: Empty response"))
            } else {
                Result.failure(Exception("Failed to free table: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createParcelOrder(
        locationId: Int,
        customerName: String,
        customerPhone: String
    ): Result<com.swadratna.swadratna_staff.data.remote.services.CreateOrderResponse> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val request = com.swadratna.swadratna_staff.data.remote.services.CreateOrderRequest(
                location_id = locationId,
                customer_name = customerName,
                customer_phone = customerPhone,
                order_type = "takeaway"
            )
            val response = apiService.createOrder(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Failed to create order: Empty response"))
            } else {
                Result.failure(Exception("Failed to create order: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordBillPayment(billId: Int, request: RecordPaymentRequest): Result<RecordPaymentResponse> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.recordBillPayment(billId, request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Failed to record payment: Empty response"))
            } else {
                Result.failure(Exception("Failed to record payment: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
