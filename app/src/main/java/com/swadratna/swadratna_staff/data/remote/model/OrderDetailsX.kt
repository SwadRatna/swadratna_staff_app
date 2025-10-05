package com.swadratna.swadratna_staff.data.remote.model

data class OrderDetailsX(
    val kots: List<KotX>,
    val location: LocationX,
    val order: OrderXX,
    val table: TableX,
    val user: UserX
)