package com.swadratna.swadratna_staff.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import com.swadratna.swadratna_staff.data.local.entities.StaffUser

import androidx.room.TypeConverters
import com.swadratna.swadratna_staff.data.local.converters.Converters

@Database(entities = [StaffUser::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun staffUserDao(): StaffUserDao
}
