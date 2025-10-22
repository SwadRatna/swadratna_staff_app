package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class PaginationInfo(
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("has_prev") val hasPrev: Boolean,
    val limit: Int,
    val page: Int,
    val total: Int,
    @SerializedName("total_pages") val totalPages: Int
)
