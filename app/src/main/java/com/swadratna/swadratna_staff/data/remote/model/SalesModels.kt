package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class SalesResponse(
    val pagination: SalesPagination,
    val sales: List<SaleTransaction>?,
    val summary: SalesSummary
)

data class SalesPagination(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("has_prev") val hasPrev: Boolean,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("total_pages") val totalPages: Int
)

data class SalesSummary(
    val count: Int,
    @SerializedName("total_amount") val totalAmount: Double
)

data class SaleTransaction(
    val id: Int,
    @SerializedName("bill_number") val billNumber: String,
    @SerializedName("order_type") val orderType: String?,
    val amount: Double,
    val status: String,
    @SerializedName("payment_mode") val paymentMode: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("edited_by") val editedBy: String?,
    @SerializedName("edited_at") val editedAt: String?
)
