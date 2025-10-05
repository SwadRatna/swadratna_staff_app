package com.swadratna.swadratna_staff.data.remote.repositories

import com.swadratna.swadratna_staff.data.remote.model.Customer
import com.swadratna.swadratna_staff.data.remote.model.CustomerBill
import com.swadratna.swadratna_staff.data.remote.model.KotRequest
import com.swadratna.swadratna_staff.data.remote.model.KotResponse

import com.swadratna.swadratna_staff.data.remote.model.MenuResponse
import com.swadratna.swadratna_staff.data.remote.model.OccupyTableRequest
import com.swadratna.swadratna_staff.data.remote.model.OccupyTableResponse
import com.swadratna.swadratna_staff.data.remote.model.OrderDetailsX
import com.swadratna.swadratna_staff.data.remote.model.TableListResponse
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import com.swadratna.swadratna_staff.data.remote.services.ApproveBillResponse
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class OrderManagementRepository @Inject constructor(
    @Named("authenticated")  private val apiService: ApiService
) {

    suspend fun getTablesByLocation(locationId: Int): Result<TableListResponse> {
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

    suspend fun approveBill(orderId: String): Result<ApproveBillResponse> {
        return try {
            val response = apiService.approveBill(orderId)
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
}
