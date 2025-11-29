package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class SalesResponse(
    val sales: List<SaleTransaction>,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("total_count") val totalCount: Int,
    val page: Int,
    val limit: Int
)

data class SaleTransaction(
    val id: Int,
    @SerializedName("bill_number") val billNumber: String,
    @SerializedName("created_at") val date: String,
    val amount: Double,
    val status: String,
    @SerializedName("order_type") val orderType: String?,
    @SerializedName("table_name") val tableName: String?,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("edited_by") val editedBy: String?,
    @SerializedName("edited_at") val editedAt: String?,
    @SerializedName("payment_mode") val paymentMode: String?
)
