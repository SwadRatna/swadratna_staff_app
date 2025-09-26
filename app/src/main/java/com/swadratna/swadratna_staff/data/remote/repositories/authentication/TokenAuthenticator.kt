package com.swadratna.swadratna_staff.data.remote.repositories.authentication

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject


/**
 * A Retrofit Authenticator that automatically refreshes the access token upon a 401 error.
 * This class is a key part of the automatic token refresh workflow and serves as a crucial fallback.
 *
 * @param tokenManager The TokenManager instance to handle token operations.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager
) : Authenticator {

    // This lock prevents multiple threads from attempting to refresh the token simultaneously
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) {
            return null
        }

        return synchronized(lock) {
            val oldAccessToken = tokenManager.getAccessToken()

            // Check if the token was already refreshed by another thread
            if (oldAccessToken != response.request.header("Authorization")?.removePrefix("Bearer ")) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $oldAccessToken")
                    .build()
            }

            // Perform the synchronous token refresh call.
            val newTokens = runBlocking { tokenManager.refreshAccessToken() }
            newTokens?.let {
                // Retry the original request with the new access token
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${it.accessToken}")
                    .build()
            } ?: run {
                // Token refresh failed, so we can't retry the request.
                null
            }
        }
    }
}
