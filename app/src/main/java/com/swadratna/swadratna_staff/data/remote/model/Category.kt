package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

// --- 1. Category Data Class ---
data class Category(
    val id: Int,
    @SerializedName("tenant_id") val tenantId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val name: String,
    val description: String,
    @SerializedName("display_order") val displayOrder: Int,
    @SerializedName("is_active") val isActive: Boolean
)

// --- 2. Menu Item Details Data Classes ---

data class NutritionalInfo(
    val calories: Int,
    val fat: String,
    val msg: String
)

data class MenuItem(
    val id: Int,
    @SerializedName("tenant_id") val tenantId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("category_id") val categoryId: Int,
    val name: String,
    val description: String,
    val price: Int,
    @SerializedName("discount_percentage") val discountPercentage: Int,
    @SerializedName("discounted_price") val discountedPrice: Double, // Use Double since it's 151.2
    val currency: String,
    val image: String,
    @SerializedName("is_vegetarian") val isVegetarian: Boolean,
    @SerializedName("is_available") val isAvailable: Boolean,
    @SerializedName("preparation_time") val preparationTime: Int,
    val ingredients: List<String>,
    @SerializedName("allergen_info") val allergenInfo: List<String>,
    @SerializedName("nutritional_info") val nutritionalInfo: NutritionalInfo,
    @SerializedName("spicy_level") val spicyLevel: Int,
    val tags: List<String>,
    @SerializedName("display_order") val displayOrder: Int
)

// --- 3. Top-Level Response Data Class ---
data class MenuResponse(
    val categories: List<Category>,
    // This maps the string category ID (e.g., "1000001") to a list of MenuItems.
    @SerializedName("menu_items") val menuItems: Map<String, List<MenuItem>>
)