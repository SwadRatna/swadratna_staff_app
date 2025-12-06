package com.swadratna.swadratna_staff.data.remote.repositories

import com.swadratna.swadratna_staff.data.remote.model.SalesResponse
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import com.swadratna.swadratna_staff.utils.network.NetworkMonitor
import javax.inject.Inject
import javax.inject.Named

class SalesRepository @Inject constructor(
    @Named("authenticated") private val apiService: ApiService,
    private val networkMonitor: NetworkMonitor
) {
    suspend fun getSales(
        date: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        locationIds: String? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
        page: Int? = 1,
        limit: Int? = 20,
        orderType: String? = null
    ): Result<SalesResponse> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.getSales(
                date = date,
                fromDate = fromDate,
                toDate = toDate,
                locationIds = locationIds,
                minAmount = minAmount,
                maxAmount = maxAmount,
                page = page,
                limit = limit,
                orderType = orderType
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
