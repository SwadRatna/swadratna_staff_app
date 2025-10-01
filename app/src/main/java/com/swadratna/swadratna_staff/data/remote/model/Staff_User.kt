package com.swadratna.swadratna_staff.data.remote.model

data class Staff_User(
    val location: Location,
    val staff: Staff,
    val token: String,
    val user: User
)