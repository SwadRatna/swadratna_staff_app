package com.swadratna.swadratna_staff.data.remote.repositories.authentication

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.swadratna.swadratna_staff.data.remote.JwtUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the secure storage and retrieval of authentication tokens for staff applications.
 * This simplified version doesn't handle token refreshing, as staff apps should logout on 401 errors.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // EncryptedSharedPreferences is the secure way to store tokens
    private val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "token_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        const val ACCESS_TOKEN_KEY = "access_token"
        const val ACCESS_TOKEN_EXPIRY_KEY = "access_token_expiry"
    }

    // Use a StateFlow to provide a thread-safe way to get the latest tokens.
    private val _accessTokenFlow = MutableStateFlow(getAccessToken())
    val accessTokenFlow: StateFlow<String?> = _accessTokenFlow

    /**
     * Saves the access token along with its expiry time to secure storage.
     * @param accessToken The new access token.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTokens(accessToken: String) {
        val expiry = JwtUtils.getExpiryTimeFromToken(accessToken)
        val currentMillis = System.currentTimeMillis()
        expiry?.let { expiry ->
            sharedPrefs.edit().apply {
                putString(ACCESS_TOKEN_KEY, accessToken)
                putLong(ACCESS_TOKEN_EXPIRY_KEY, currentMillis + (expiry * 1000L))
                apply()
            }
            _accessTokenFlow.value = accessToken
        }
    }

    /**
     * Clears the stored access token, effectively logging the user out.
     */
    fun clearTokens() {
        sharedPrefs.edit().apply {
            remove(ACCESS_TOKEN_KEY)
            remove(ACCESS_TOKEN_EXPIRY_KEY)
            apply()
        }
        _accessTokenFlow.value = null
    }

    /**
     * Retrieves the stored access token.
     */
    fun getAccessToken(): String? {
        return sharedPrefs.getString(ACCESS_TOKEN_KEY, null)
    }

    /**
     * Retrieves the stored access token expiry timestamp in milliseconds.
     */
    fun getAccessTokenExpiryTime(): Long {
        return sharedPrefs.getLong(ACCESS_TOKEN_EXPIRY_KEY, 0L)
    }

    /**
     * Checks if the access token is expiring within the next 5 minutes.
     * @return True if the token is expiring soon, false otherwise.
     */
    fun isAccessTokenExpiringSoon(): Boolean {
        val expiryTime = getAccessTokenExpiryTime()
        // If there is no token or it's already expired, this will return true.
        if (expiryTime == 0L) return true
        val timeToExpire = expiryTime - System.currentTimeMillis()
        return timeToExpire < TimeUnit.MINUTES.toMillis(5)
    }
}