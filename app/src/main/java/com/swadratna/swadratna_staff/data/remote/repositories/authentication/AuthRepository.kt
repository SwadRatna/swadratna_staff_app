package com.swadratna.swadratna_staff.data.remote.repositories.authentication

import javax.inject.Inject

/**
 * A repository that uses the TokenManager to perform a proactive token refresh.
 * This logic should be triggered before any critical API calls.
 */
class AuthRepository @Inject constructor(
    private val tokenManager: TokenManager
) {
    /**
     * Checks if the access token is expiring soon and refreshes it if needed.
     * This function should be called before making any API requests.
     */
    suspend fun ensureAccessTokenIsValid() {
        if (tokenManager.isAccessTokenExpiringSoon()) {
            tokenManager.refreshAccessToken()
        }
    }
}