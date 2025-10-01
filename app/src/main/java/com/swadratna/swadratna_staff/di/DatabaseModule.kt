package com.swadratna.swadratna_staff.di

import android.content.Context
import androidx.room.Room
import com.swadratna.swadratna_staff.data.local.AppDatabase
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "swadratna_staff_db"
        ).build()
    }

    @Provides
    fun provideStaffUserDao(appDatabase: AppDatabase): StaffUserDao {
        return appDatabase.staffUserDao()
    }
}
