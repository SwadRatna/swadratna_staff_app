package com.swadratna.swadratna_staff.data.remote.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import com.swadratna.swadratna_staff.data.local.entities.StaffUser
import com.swadratna.swadratna_staff.data.remote.model.Staff_User
import com.swadratna.swadratna_staff.data.remote.repositories.authentication.TokenManager
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import com.swadratna.swadratna_staff.data.remote.services.LoginRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import com.swadratna.swadratna_staff.utils.network.NetworkMonitor

/**
 * Repository for authentication operations in staff applications.
 * For staff apps, token refreshing is not implemented - users are logged out on 401 errors.
 */
@Singleton
class AuthRepository @Inject constructor(
    @Named("unauthenticated") private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val staffUserDao: StaffUserDao,
    private val networkMonitor: NetworkMonitor
) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun login(loginRequest: LoginRequest): Result<Staff_User> {
        if (!networkMonitor.isOnline.value) {
            return Result.failure(Exception("No network connection"))
        }
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful) {
                response.body()?.let {
                    tokenManager.saveTokens(it.token)
                    Result.success(it)
                } ?: Result.failure(Exception("Login failed: Empty response"))
            } else {
                val errorBody = response.errorBody()?.string()

                val errorMessage = if (errorBody != null) {
                    try {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.errorMessage
                    } catch (e: Exception) {
                        "Login failed with code ${response.code()}"
                    }
                } else {
                    "Login failed with code ${response.code()}"
                }

                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenManager.getAccessToken() != null
    }

    suspend fun logout() {
        tokenManager.clearTokens()
    }

    fun getStaffUser(): Flow<StaffUser?> {
        return staffUserDao.getLoggedInStaffUser()
    }
}
