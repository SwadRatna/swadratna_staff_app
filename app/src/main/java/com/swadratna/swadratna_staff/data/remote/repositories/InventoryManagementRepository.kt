package com.swadratna.swadratna_staff.data.remote.repositories

import com.swadratna.swadratna_staff.data.remote.model.MenuItem
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import com.swadratna.swadratna_staff.data.remote.services.AvailabilityRequest
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

// Sealed class to represent the result of an API call
sealed class ApiResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : ApiResult<T>()
    data class Error(val exception: Exception) : ApiResult<Nothing>()
}

class InventoryManagementRepository @Inject constructor(
    @Named("authenticated") private val apiService: ApiService
) {

    // Fetches the menu for a given location and returns a wrapped result
    suspend fun getMenuByLocation(locationId: Int): ApiResult<List<MenuItem>> {
        return try {
            val response = apiService.getMenuByLocation(locationId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                // Handle non-2xx HTTP responses (e.g., 404, 500)
                ApiResult.Error(HttpException(response))
            }
        } catch (e: IOException) {
            // Handle network exceptions (e.g., no internet connection)
            ApiResult.Error(e)
        } catch (e: Exception) {
            // Handle any other unexpected exceptions
            ApiResult.Error(e)
        }
    }


    suspend fun updateMenuItemAvailability(locationId: Int, menuId: Int, isAvailable: Boolean): ApiResult<Unit> {
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