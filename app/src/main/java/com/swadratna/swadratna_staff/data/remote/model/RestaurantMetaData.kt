package com.swadratna.swadratna_staff.data.remote.model

data class RestaurantMetaData(
    val restuarentId: Int,
    val restaurantName: String,
    val location: String,
    val tables: List<Table>,
    val menuItems: List<MenuItem>
)