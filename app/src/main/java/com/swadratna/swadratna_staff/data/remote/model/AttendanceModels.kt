package com.swadratna.swadratna_staff.data.remote.model

data class AttendanceCheckInRequest(
    val staff_id: Int
)

data class AttendanceCheckOutRequest(
    val staff_id: Int
)

data class AttendanceModifyRequest(
    val check_in_time: String? = null,
    val check_out_time: String? = null,
    val status: String? = null,
    val notes: String? = null,
    val is_late: Boolean? = null
)

data class CurrentAttendance(
    val id: Int,
    val staff_id: Int,
    val check_in_time: String?,
    val check_out_time: String?,
    val status: String?,
    val date: String?
)
