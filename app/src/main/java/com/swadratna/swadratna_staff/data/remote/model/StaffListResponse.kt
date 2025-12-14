package com.swadratna.swadratna_staff.data.remote.model

import com.google.gson.annotations.SerializedName

data class StaffListResponse(
    @SerializedName("staff") val staff: List<Staff>
)
