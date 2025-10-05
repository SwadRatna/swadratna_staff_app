package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class KotResponse(
    @SerializedName("message") val message: String,
    val kot: Kot,
    val kot_items: List<KotItem>,
    val order: OrderX
)
