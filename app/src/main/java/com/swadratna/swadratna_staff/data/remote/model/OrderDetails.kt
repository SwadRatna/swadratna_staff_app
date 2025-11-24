package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class OrderDetails(
    val id: Int,
    @SerializedName("tenant_id") val tenantId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("table_id") val tableId: Int,
    @SerializedName("location_id") val locationId: Int,
    @SerializedName("restaurant_id") val restaurantId: Int,
    @SerializedName("total_value") val totalValue: Double,
    @SerializedName("order_date") val orderDate: String,
    @SerializedName("order_value") val orderValue: Double,
    val discount: Double,
    @SerializedName("order_status") val orderStatus: String
)

data class TableOccupancyDetails(
    val id: Int,
    @SerializedName("tenant_id") val tenantId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("table_id") val tableId: Int,
    @SerializedName("location_id") val locationId: Int,
    @SerializedName("restaurant_id") val restaurantId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("occupied_from") val occupiedFrom: String,
    @SerializedName("occupied_to") val occupiedTo: String,
    val status: String
)