package com.swadratna.swadratna_staff.data.remote.model

data class OrderDetailsX(
    val kots: List<KotX>?,
    val location: LocationX,
    val order: OrderXX,
    val table: TableX,
    val user: UserX?,
    val bill: OrderBill?,
    val bill_generated: Boolean?
)

data class OrderBill(
    val bill_number: String,
    val id: Int,
    val total_amount: Double
)