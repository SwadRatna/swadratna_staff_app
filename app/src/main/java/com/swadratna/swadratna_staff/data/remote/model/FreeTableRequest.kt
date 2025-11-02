package com.swadratna.swadratna_staff.data.remote.model

data class FreeTableRequest(
    val cancel_order: Boolean,
    val reason: String
)

data class FreeTableResponse(
    val success: Boolean,
    val message: String,

)