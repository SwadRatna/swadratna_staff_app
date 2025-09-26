package com.swadratna.swadratna_staff.data.remote.repositories

import com.swadratna.swadratna_staff.data.remote.model.TokenRefreshRequest
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: ApiService // Inject ApiService directly for refresh token call
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val accessToken = runBlocking { tokenManager.getAccessToken() }
        val refreshToken = runBlocking { tokenManager.getRefreshToken() }

        // If no access token or refresh token, or if the request already has a new token, give up.
        if (accessToken == null || refreshToken == null || response.request.header("Authorization") != "Bearer $accessToken") {
            return null
        }

        // Synchronously refresh the token
        val newAccessToken = runBlocking {
            val refreshResponse = apiService.refreshToken(TokenRefreshRequest(refreshToken))
            if (refreshResponse.isSuccessful) {
                refreshResponse.body()?.accessToken?.also {
                    tokenManager.saveTokens(it, refreshResponse.body()!!.refreshToken)
                }
            } else {
                tokenManager.clearTokens() // Clear tokens if refresh fails
                null
            }
        }

        return if (newAccessToken != null) {
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            null // Give up, token refresh failed or no new token
        }
    }
}
