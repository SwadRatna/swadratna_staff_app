package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class OrderListItem(
    @SerializedName("created_at") val createdAt: String,
    val discount: Double,
    val id: Int,
    @SerializedName("location_id") val locationId: Int,
    @SerializedName("order_date") val orderDate: String,
    @SerializedName("order_status") val orderStatus: String,
    @SerializedName("order_value") val orderValue: Double,
    @SerializedName("restaurant_id") val restaurantId: Int,
    val table: TableX, // Reusing TableX
    @SerializedName("table_id") val tableId: Int,
    @SerializedName("total_value") val totalValue: Double,
    @SerializedName("updated_at") val updatedAt: String,
    val user: UserX, // Reusing UserX
    @SerializedName("user_id") val userId: Int
)
