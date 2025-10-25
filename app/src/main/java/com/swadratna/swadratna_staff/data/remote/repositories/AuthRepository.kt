package com.swadratna.swadratna_staff.data.remote.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import com.swadratna.swadratna_staff.data.local.entities.StaffUser
import com.swadratna.swadratna_staff.data.remote.model.Staff_User
import com.swadratna.swadratna_staff.data.remote.model.TokenRefreshRequest
import com.swadratna.swadratna_staff.data.remote.model.TokenRefreshResponse
import com.swadratna.swadratna_staff.data.remote.repositories.authentication.TokenManager
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import com.swadratna.swadratna_staff.data.remote.services.LoginRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @Named("unauthenticated") private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val staffUserDao: StaffUserDao
) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun login(loginRequest: LoginRequest): Result<Staff_User> {
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful) {
                response.body()?.let {
                    tokenManager.saveTokens(it.token)
                    Result.success(it)
                } ?: Result.failure(Exception("Login failed: Empty response"))
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshToken(refreshToken: String): Result<TokenRefreshResponse> {
        return try {
            val response = apiService.refreshToken(TokenRefreshRequest(refreshToken))
            if (response.isSuccessful) {
                response.body()?.let {
                    tokenManager.saveTokens(it.accessToken)
                    Result.success(it)
                } ?: Result.failure(Exception("Token refresh failed: Empty response"))
            }
            else {
                tokenManager.clearTokens() // Clear tokens if refresh fails
                Result.failure(Exception("Token refresh failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            tokenManager.clearTokens() // Clear tokens on network error during refresh
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
