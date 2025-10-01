package com.swadratna.swadratna_staff.data.remote

import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONObject
import java.util.Base64
import java.util.concurrent.TimeUnit

object JwtUtils {

    /**
     * Decodes a JWT token's payload to extract the 'exp' (expiration) claim.
     * The 'exp' claim is a Unix timestamp in seconds.
     *
     * @param token The JWT access token (e.g., "header.payload.signature").
     * @return The expiration timestamp in **milliseconds**, or null if decoding fails.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getExpiryTimeFromToken(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null // Must have at least header and payload

            val payloadBase64 = parts[1]

            // JWT uses Base64URL encoding, which is why we use Base64.getUrlDecoder()
            // We must also handle padding manually for robustness if standard Base64 is used.
            // Using java.util.Base64 is fine for JVM/non-Android specific Kotlin.
            val decoder = Base64.getUrlDecoder()
            val decodedBytes = decoder.decode(payloadBase64)
            val payloadJson = String(decodedBytes, Charsets.UTF_8)

            val jsonObject = JSONObject(payloadJson)

            // 'exp' is a Unix timestamp in seconds.
            val expirySeconds = jsonObject.optLong("exp", 0)

            // Convert seconds to milliseconds for easy comparison with System.currentTimeMillis()
            if (expirySeconds > 0) TimeUnit.SECONDS.toMillis(expirySeconds) else null
        } catch (e: Exception) {
            // Log the error (e.g., if JSON is malformed or Base64 decoding fails)
            e.printStackTrace()
            null
        }
    }
}