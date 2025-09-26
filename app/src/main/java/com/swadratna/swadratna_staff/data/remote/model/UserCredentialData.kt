package com.swadratna.swadratna_staff.data.remote.model

data class UserCredentialData(
    val userName: String,
    val userMail: String,
    val password: String,
    val role: String,
    val assignedRestaurant: RestaurantMetaData,
)