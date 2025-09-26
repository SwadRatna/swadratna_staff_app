package com.swadratna.swadratna_staff.data.remote.model

data class MenuItem(
    val id: Int,
    val name: String,
    val category: Category,
    var isAvailable: Boolean,
    val price: Int,
    val disCountPercentage: Int,
    val discountedPrice: Int
)

data class Category(
    val categoryId: Int,
    val categoryName: String
)