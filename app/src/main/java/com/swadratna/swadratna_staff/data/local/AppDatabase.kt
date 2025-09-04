package com.swadratna.swadratna_staff.data.local

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Entity(tableName = "dummy")
data class DummyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Database(
    entities = [DummyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dummyDao(): DummyDao
}
