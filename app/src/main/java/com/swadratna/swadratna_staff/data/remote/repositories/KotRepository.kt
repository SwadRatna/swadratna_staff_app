package com.swadratna.swadratna_staff.data.remote.repositories

import com.swadratna.swadratna_staff.data.remote.model.KotListResponse
import com.swadratna.swadratna_staff.data.remote.model.KotStatusUpdateRequest
import com.swadratna.swadratna_staff.data.remote.model.KotStatusUpdateResponse
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class KotRepository @Inject constructor(
    @Named("authenticated") private val apiService: ApiService
) {

    suspend fun getKots(locationId: Int, pendingOnly: Boolean? = null): Result<KotListResponse> {
        return try {
            val response = apiService.getKots(locationId)
            if (response.isSuccessful) {
                response.body()?.let { kotListResponse ->
                    Result.success(kotListResponse)
                } ?: Result.failure<KotListResponse>(Exception("No KOTs found"))
            } else {
                Result.failure<KotListResponse>(Exception("Failed to fetch KOTs: ${response.message()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error while fetching KOTs. Please check connection.", e))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error while fetching KOTs: ${e.message()}", e))
        } catch (e: Exception) {
            Result.failure(Exception("An unexpected error occurred during KOT fetching.", e))
        }
    }

    suspend fun updateKotStatus(kotId: Int, newStatus: String): Result<KotStatusUpdateResponse> {
        return try {
            val request = KotStatusUpdateRequest(status = newStatus)
            val response = apiService.updateKotStatus(kotId, request)
            if (response.isSuccessful) {
                response.body()?.let { updateResponse ->
                    Result.success(updateResponse)
                } ?: Result.failure<KotStatusUpdateResponse>(Exception("Empty response"))
            } else {
                Result.failure<KotStatusUpdateResponse>(Exception("Failed to update KOT status: ${response.message()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error while updating KOT status. Please check connection.", e))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error while updating KOT status: ${e.message()}", e))
        } catch (e: Exception) {
            Result.failure(Exception("An unexpected error occurred during KOT status update.", e))
        }
    }
}