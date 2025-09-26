package com.swadratna.swadratna_staff.data.remote.model

data class CustomerBill(
    val orderId: String,
    val customerName: String,
    val order: List<OrderRequest>,
    val items: List<String>,
    val status: String
)

data class OrderRequest(
    val orderItem: List<OrderItem>
)

data class OrderItem(
    val items: MenuItem,
    val quantity: Int,
)