package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class Bill(
    val id: Int,
    @SerializedName("tenant_id") val tenantId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("table_id") val tableId: Int,
    @SerializedName("location_id") val locationId: Int,
    @SerializedName("restaurant_id") val restaurantId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("bill_number") val billNumber: String,
    @SerializedName("sub_total") val subTotal: Double,
    @SerializedName("tax_amount") val taxAmount: Double,
    @SerializedName("service_charge") val serviceCharge: Double,
    @SerializedName("discount_amount") val discountAmount: Double,
    @SerializedName("total_amount") val totalAmount: Double,
    val status: String,
    @SerializedName("created_by_id") val createdById: Int
)
