package com.swadratna.swadratna_staff.data.remote.model

/**
 * Enum class representing the different staff roles, paired with their case-sensitive string value.
 */
enum class StaffRole(val roleName: String) {
    MANAGER("manager"),
    WAITER("waiter"),
    CHEF("chef"),
    CASHIER("cashier"); // Don't forget the semicolon if you have other properties/functions below
    
    /**
     * Finds the StaffRole enum constant corresponding to the case-sensitive role string.
     * Returns null if no match is found.
     */
    companion object {
        fun fromString(roleString: String): StaffRole? {
            return entries.find { it.roleName == roleString }
        }
    }
}