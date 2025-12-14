package com.swadratna.swadratna_staff.data.remote.repositories

import com.swadratna.swadratna_staff.data.remote.model.Staff
import com.swadratna.swadratna_staff.data.remote.model.AttendanceCheckInRequest
import com.swadratna.swadratna_staff.data.remote.model.AttendanceCheckOutRequest
import com.swadratna.swadratna_staff.data.remote.model.AttendanceModifyRequest
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import com.swadratna.swadratna_staff.utils.network.NetworkMonitor
import android.util.Log
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class StaffRepository @Inject constructor(
    @Named("authenticated") private val apiService: ApiService,
    private val networkMonitor: NetworkMonitor
) {
    suspend fun getStaffMembers(): Result<List<Staff>> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.getStaffMembers()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.staff)
                } ?: Result.failure(Exception("Staff list empty"))
            } else {
                Result.failure(Exception("Failed to fetch staff members: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkIn(staffId: Int): Result<String> {
        if (!networkMonitor.isOnline.value) return Result.failure(Exception("No network connection"))
        return try {
            val response = apiService.checkIn(AttendanceCheckInRequest(staffId))
            if (response.isSuccessful) {
                Log.d("StaffRepository", "Check-in successful for staffId: $staffId")
                Result.success("Check-in successful")
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Log.e("StaffRepository", "Check-in failed for staffId: $staffId. Error: $errorMsg, Code: ${response.code()}")
                Result.failure(Exception("Check-in failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("StaffRepository", "Check-in exception for staffId: $staffId", e)
            Result.failure(e)
        }
    }

    suspend fun checkOut(staffId: Int): Result<String> {
        if (!networkMonitor.isOnline.value) return Result.failure(Exception("No network connection"))
        return try {
            val response = apiService.checkOut(AttendanceCheckOutRequest(staffId))
            if (response.isSuccessful) {
                Log.d("StaffRepository", "Check-out successful for staffId: $staffId")
                Result.success("Check-out successful")
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Log.e("StaffRepository", "Check-out failed for staffId: $staffId. Error: $errorMsg, Code: ${response.code()}")
                Result.failure(Exception("Check-out failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("StaffRepository", "Check-out exception for staffId: $staffId", e)
            Result.failure(e)
        }
    }

    suspend fun modifyAttendance(id: Int, request: AttendanceModifyRequest): Result<String> {
        if (!networkMonitor.isOnline.value) return Result.failure(Exception("No network connection"))
        return try {
            val response = apiService.modifyAttendance(id, request)
            if (response.isSuccessful) {
                Log.d("StaffRepository", "Attendance modified successfully for id: $id")
                Result.success("Attendance modified successfully")
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Log.e("StaffRepository", "Modify attendance failed for id: $id. Error: $errorMsg, Code: ${response.code()}")
                Result.failure(Exception("Failed to modify attendance: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("StaffRepository", "Modify attendance exception for id: $id", e)
            Result.failure(e)
        }
    }
}
