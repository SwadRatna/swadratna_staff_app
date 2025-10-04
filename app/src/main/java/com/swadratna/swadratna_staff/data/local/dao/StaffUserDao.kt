package com.swadratna.swadratna_staff.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swadratna.swadratna_staff.data.local.entities.StaffUser
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffUser(staffUser: StaffUser)

    @Query("SELECT * FROM staff_users WHERE id = :userId")
    fun getStaffUser(userId: String): Flow<StaffUser?>

    @Query("SELECT * FROM staff_users LIMIT 1") // New function
    fun getLoggedInStaffUser(): Flow<StaffUser?>

    @Query("DELETE FROM staff_users")
    suspend fun clearStaffUsers()
}
