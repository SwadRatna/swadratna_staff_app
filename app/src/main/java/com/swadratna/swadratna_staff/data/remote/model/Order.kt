package com.swadratna.swadratna_staff.data.remote.model

data class Order(
    val orderNumber: String,
    val customerName: String,
    val items: List<String>,
    val status: String
)