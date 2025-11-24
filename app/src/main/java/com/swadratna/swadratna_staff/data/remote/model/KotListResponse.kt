package com.swadratna.swadratna_staff.data.remote.model

data class KotListResponse(
    val kots: List<KotItemX>,
    val location_id: Int,
    val total: Double
)

data class KotStatusUpdateResponse(
    val success: Boolean,
    val message: String,
    val data: KotItem? = null
)

data class KotStatusUpdateRequest(
    val status: String
)


data class ItemX(
    val id: Int,
    val instructions: String,
    val menu_item: MenuItemX,
    val menu_item_id: Int,
    val quantity: Int,
    val status: String
)


data class MenuItemX(
    val category_id: Int,
    val created_at: String,
    val currency: String,
    val description: String,
    val display_order: Int,
    val id: Int,
    val is_available: Boolean,
    val is_vegetarian: Boolean,
    val name: String,
    val preparation_time: Int,
    val price: Double,
    val tenant_id: Int,
    val updated_at: String
)


data class TableXX(
    val capacity: Int,
    val created_at: String,
    val id: Int,
    val is_occupied: Boolean,
    val location_id: Int,
    val qr_code_img_url: String,
    val status: String,
    val table_id: String,
    val tenant_id: Int,
    val updated_at: String
)


data class KotItemX(
    val created_at: String,
    val customer_name: String,
    val id: Int,
    val items: List<ItemX>,
    val kot_number: String,
    val order_id: Int,
    val prep_time_mins: Any,
    val status: String,
    val table: TableXX
)