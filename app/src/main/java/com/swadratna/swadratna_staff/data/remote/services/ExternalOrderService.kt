package com.swadratna.swadratna_staff.data.remote.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class CreateOrderRequest(
    val location_id: Int,
    val customer_name: String,
    val customer_phone: String,
    val order_type: String
)

data class Address(
    val plot_no: String?,
    val po_box_no: String?,
    val street_1: String?,
    val street_2: String?,
    val locality: String?,
    val city: String?,
    val pincode: String?,
    val landmark: String?
)

data class LocationInfo(
    val address: Address?,
    val id: Int,
    val name: String?,
    val restaurant_id: Int?
)

data class CreatedOrder(
    val created_at: String?,
    val customer_email: String?,
    val customer_name: String?,
    val customer_phone: String?,
    val id: Int,
    val location_id: Int,
    val order_type: String?,
    val restaurant_id: Int?,
    val special_notes: String?,
    val status: String?,
    val user_id: Int?
)

data class CreateOrderResponse(
    val location: LocationInfo?,
    val message: String?,
    val order: CreatedOrder
)