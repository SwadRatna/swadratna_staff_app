package com.swadratna.swadratna_staff.data.remote.repositories

import com.swadratna.swadratna_staff.data.remote.model.TokenRefreshRequest
import com.swadratna.swadratna_staff.data.remote.model.TokenRefreshResponse
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import com.swadratna.swadratna_staff.data.remote.services.LoginRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(loginRequest: LoginRequest): Result<TokenRefreshResponse> {
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful) {
                response.body()?.let {
                    tokenManager.saveTokens(it.accessToken, it.refreshToken)
                    Result.success(it)
                } ?: Result.failure(Exception("Login failed: Empty response"))
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<TokenRefreshResponse> {
        return try {
            val response = apiService.refreshToken(TokenRefreshRequest(refreshToken))
            if (response.isSuccessful) {
                response.body()?.let {
                    tokenManager.saveTokens(it.accessToken, it.refreshToken)
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
}
