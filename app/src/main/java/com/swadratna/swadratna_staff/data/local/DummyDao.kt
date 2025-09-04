package com.swadratna.swadratna_staff.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DummyDao {
    @Insert
    suspend fun insert(dummy: DummyEntity)

    @Query("SELECT * FROM dummy")
    suspend fun getAll(): List<DummyEntity>
}
