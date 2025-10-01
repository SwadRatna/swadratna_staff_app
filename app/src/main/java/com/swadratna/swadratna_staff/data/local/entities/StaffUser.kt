package com.swadratna.swadratna_staff.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.swadratna.swadratna_staff.data.remote.model.Address
import com.swadratna.swadratna_staff.data.remote.model.Location // Assuming this is the remote Location model

@Entity(tableName = "staff_users")
data class StaffUser(
    @PrimaryKey val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val location: Location, // Changed from LocationEntity to Location
    val phone: String,
    val permission:List<String>
)
