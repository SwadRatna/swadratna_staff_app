package com.swadratna.swadratna_staff.data.remote.model

// Data models for API requests/responses. The expiry times are now included.
data class TokenRefreshRequest(
    val refreshToken: String
)

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long, // Lifespan of the access token in seconds
    val refreshExpiresIn: Long // Lifespan of the refresh token in seconds
)