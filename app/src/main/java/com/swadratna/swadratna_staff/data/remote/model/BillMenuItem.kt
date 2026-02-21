package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class BillMenuItem(
    @SerializedName("allergen_info") val allergenInfo: List<String>?,
    @SerializedName("category_id") val categoryId: Int,
    val currency: String,
    val description: String,
    @SerializedName("discounted_price") val discountedPrice: Double?,
    val id: Int,
    val image: String,
    val ingredients: List<String>?,
    @SerializedName("is_available") val isAvailable: Boolean,
    @SerializedName("is_vegetarian") val isVegetarian: Boolean,
    val name: String,
    @SerializedName("nutritional_info") val nutritionalInfo: BillNutritionalInfo?,
    @SerializedName("preparation_time") val preparationTime: Long,
    val price: Double,
    @SerializedName("spicy_level") val spicyLevel: Int?,
    val tags: List<String>?
)
