package com.swadratna.swadratna_staff.data.remote.repositories

import com.swadratna.swadratna_staff.data.remote.model.MenuResponse
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import com.swadratna.swadratna_staff.data.remote.services.AvailabilityRequest
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import com.swadratna.swadratna_staff.utils.network.NetworkMonitor

// Sealed class to represent the result of an API call
sealed class ApiResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : ApiResult<T>()
    data class Error(val exception: Exception) : ApiResult<Nothing>()
}

class InventoryManagementRepository @Inject constructor(
    @Named("authenticated") private val apiService: ApiService,
    private val networkMonitor: NetworkMonitor
) {

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


    suspend fun updateMenuItemAvailability(locationId: Int, menuId: Int, isAvailable: Boolean): ApiResult<Unit> {
        if (!networkMonitor.isOnline.value) {
            return ApiResult.Error(Exception("No network connection"))
        }
        return try {
            val requestBody = AvailabilityRequest(isAvailable)
            val response = apiService.updateMenuItemAvailability(locationId.toString(), menuId.toString(), requestBody)

            if (response.isSuccessful) {
                // Return success without data, as the response body might be empty or not needed
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(HttpException(response))
            }
        } catch (e: IOException) {
            ApiResult.Error(e)
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }
}
