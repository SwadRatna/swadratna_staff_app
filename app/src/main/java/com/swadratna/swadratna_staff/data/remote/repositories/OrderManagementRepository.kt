package com.swadratna.swadratna_staff.data.remote.repositories

import com.swadratna.swadratna_staff.data.remote.model.CustomerBill
import com.swadratna.swadratna_staff.data.remote.model.MenuItem
import com.swadratna.swadratna_staff.data.remote.model.Table
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import com.swadratna.swadratna_staff.data.remote.services.KotRequest
import com.swadratna.swadratna_staff.data.remote.services.KotResponse
import com.swadratna.swadratna_staff.data.remote.services.ApproveBillResponse
import javax.inject.Inject
import javax.inject.Named

class OrderManagementRepository @Inject constructor(
    @Named("authenticated")  private val apiService: ApiService
) {

    suspend fun getTablesByLocation(locationId: Int): Result<List<Table>> {
        return try {
            val response = apiService.getTablesByLocation(locationId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("No tables found"))
            } else {
                Result.failure(Exception("Failed to fetch tables: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMenuByLocation(locationId: Int): Result<List<MenuItem>> {
        return try {
            val response = apiService.getMenuByLocation(locationId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("No menu items found"))
            } else {
                Result.failure(Exception("Failed to fetch menu: ${response.message()}"))
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
}
