package com.swadratna.swadratna_staff.data.remote.repositories.authentication

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject


/**
 * A Retrofit Authenticator that handles 401 unauthorized responses for staff applications.
 * For staff apps, we don't refresh tokens - we simply clear tokens and let the app redirect to login.
 *
 * @param tokenManager The TokenManager instance to handle token operations.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) {
            return null
        }

        // For staff applications, we don't refresh tokens on 401 errors.
        // Instead, we clear the tokens which will trigger a logout flow.
        // This is more appropriate for staff apps where session management
        // should be simpler and more secure.
        tokenManager.clearTokens()
        
        // Return null to indicate we cannot retry the request.
        // The calling code should handle the authentication failure.
        return null
    }
}
